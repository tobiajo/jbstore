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
  spawn(fun() -> loop([], 0, 0) end).

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

loop(View, Seq, Rid) ->
  io:format("FE - loop: Seq: ~b~n", [Seq]),
  receive
    {join, Worker} ->
      loop([Worker|View], Seq, Rid);
    {put, Key, Value, Src} ->
      NewSeq = Seq + 1,
      awrite(View, Key, Value, NewSeq, Rid),
      Src ! ok,
      loop(View, NewSeq, Rid + 1);
    {get, Key, Src} ->
      case aread(View, Key, Rid) of
       false -> Src ! notfound,
       loop(View, Seq, Rid + 1);
       {Key, {NewSeq, Value}} ->
         awrite(View, Key, Value, NewSeq + 1, Rid + 1),
         Src ! {value, Key, Value},
         loop(View, NewSeq + 1, Rid + 1)
      end
  end.

aread(View, Key, Rid) ->
  lists:foreach(fun(Worker) -> Worker ! {aread, Key, Rid} end, View),

  Responses = waitread((erlang:length(View) div 2) + 1, [], Rid),
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
waitread(0, Responses, _) ->
  Responses;
waitread(WaitingFor, Responses, Rid) ->
  receive
    {value, V, Rid} -> waitread(WaitingFor - 1, [V|Responses], Rid);
    %Throwaway unexpected messages
    _ -> waitread(WaitingFor, Responses, Rid)
  end.


awrite(View, Key, Value, Seq, Rid) ->
  lists:foreach(fun(Worker) -> Worker ! {awrite, Key, Value, Seq, Rid} end, View),
  waitack((erlang:length(View) div 2) + 1, Rid).

waitack(0, _) ->
  ok;

waitack(WaitingFor, Rid) ->
  receive
    {Rid, ack} -> waitack(WaitingFor - 1, Rid);
    %Throwaway unexpected messages
    _ -> waitack(WaitingFor, Rid)
  end.

