%%%-------------------------------------------------------------------
%%% @author aganom
%%% @copyright (C) 2016, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 28. Feb 2016 14:03
%%%-------------------------------------------------------------------
-module(ar).
-author("aganom").

%% API
-export([start/2, debugGet/2]).

start(Id, FrontEnd) ->
  spawn(fun() -> init(Id, FrontEnd) end).

init(Id, FrontEnd) ->
  join(FrontEnd),
  loop(Id, storage:create(), FrontEnd).

join(FrontEnd) ->
  FrontEnd ! {join, self()}.

ack(Rid, FrontEnd) ->
  FrontEnd ! {Rid, ack}.
% Id of register
%
loop(Id, Store, FrontEnd) ->
  %io:format("~b: p: ~p s: ~p~n", [Id, Predecessor, Successor]),
  receive

  % atomic write
    {awrite, Key, Value, NewSeq, Rid} ->
      io:format("(AR~b) awrite received: NewSeq = ~b Rid = ~b~n", [Id, NewSeq, Rid]),
      Old = storage:lookup(Key, Store),
      case Old of
        false -> NewStore = storage:add(Key, {NewSeq, Value}, Store);
        {Key, {OldSeq,_}} -> if
                              OldSeq < NewSeq -> NewStore = storage:add(Key, {NewSeq, Value}, Store);
                              true -> NewStore = Store
                            end
      end,
      ack(Rid, FrontEnd),
      loop(Id, NewStore, FrontEnd);

  % atomic read
    {aread, Key, Rid} ->
      FrontEnd ! {value, storage:lookup(Key, Store), Rid},
      loop(Id, Store, FrontEnd);

  % debug get
    {debug, Key, Src} ->
      Src ! storage:lookup(Key, Store),
      loop(Id, Store, FrontEnd)
  end.


debugGet(Key, Ar) ->
  Ar ! {debug, Key, self()},
  receive
    X -> X
  end.
