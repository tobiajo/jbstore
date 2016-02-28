%%%-------------------------------------------------------------------
%%% @author aganom
%%% @copyright (C) 2016, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 28. Feb 2016 14:10
%%%-------------------------------------------------------------------
-module(frontend).
-author("aganom").

%% API
-export([start/0, put/3, get/2]).

start() ->
  spawn(fun() -> loop([], 0) end).

put(Key, Value, FE) ->
  FE ! {put, Key, Value, self()},
  receive
    ok -> ok
  end.

get(Key, FE) ->
  FE ! {get, Key, self()},
  receive
    notfound -> "not found";
    {value, Key, Value} -> {Key, Value}
  end.

loop(View, Seq) ->
  io:format("FE - loop: Seq: ~b~n", [Seq]),
  receive
    {join, Worker} ->
      loop([Worker|View], Seq);
    {put, Key, Value, Src} ->
      NewSeq = Seq + 1,
      awrite(View, Key, Value, NewSeq),
      Src ! ok,
      loop(View, NewSeq);
    {get, Key, Src} ->
      case aread(View, Key) of
       false -> Src ! notfound,
       loop(View, Seq);
       {Key, {NewSeq, Value}} ->
         awrite(View, Key, Value, NewSeq + 1),
         Src ! {value, Key, Value},
         loop(View, NewSeq + 1)
      end
  end.

aread(View, Key) ->
  lists:foreach(fun(Worker) -> Worker ! {aread, Key} end, View),

  Responses = waitread(erlang:length(View), []),
  case Responses of
    [] -> false;
    List -> List1 = removeFalses(List),
      if
        erlang:length(List1) > 0 -> getNewest(List1);
        true -> false
      end
  end.

removeFalses(Responses) ->
  removeFalsesHelper(Responses, []).

removeFalsesHelper([], Fine) ->
  Fine;
removeFalsesHelper([H|T], Fine) ->
  case H of
    false ->
      removeFalsesHelper(T, Fine);
    _ ->
      removeFalsesHelper(T, [H|Fine])
  end.

getNewest(Responses) ->
  lists:foldr(
    fun({Key1, {Seq1, Val1}}, {Key2, {Seq2, Val2}}) -> if
                                                         Seq1 > Seq2 -> {Key1, {Seq1, Val1}};
                                                         true -> {Key2, {Seq2, Val2}}
                                                       end end,
    {dummy, {0, dummy}}, Responses).
%return list of received aread values
waitread(0, Responses) ->
  Responses;
waitread(WaitingFor, Responses) ->
  receive
    {value, V} -> waitread(WaitingFor - 1, [V|Responses])
  end.


awrite(View, Key, Value, Seq) ->
  lists:foreach(fun(Worker) -> Worker ! {awrite, Key, Value, Seq} end, View),
  waitack(erlang:length(View)).

waitack(0) ->
  ok;

waitack(WaitingFor) ->
  receive
    ack -> waitack(WaitingFor - 1)
  end.

