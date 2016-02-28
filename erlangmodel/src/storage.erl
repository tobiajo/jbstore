%%%-------------------------------------------------------------------
%%% @author aganom
%%% @copyright (C) 2015, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 12. Oct 2015 15:14
%%%-------------------------------------------------------------------
-module(storage).
-author("aganom").

%% API
-export([create/0, lookup/2, split/3, merge/2, add/3]).

%create a new store
create() ->
  [].

%add a key value pair, return the updated store.
add(Key, Value, Store) ->
  Taken = lists:keytake(Key, 1, Store),
  case Taken of
    false -> [{Key, Value}|Store];
    {_, _, Rest} -> [{Key, Value}|Rest]
  end.

%return a tuple {Key, Value} or the atom false.
lookup(Key, Store) ->
  lists:keyfind(Key, 1, Store).

%return a tuple {Requested, Rest} where the updated store
%only contains the key-value pairs requested
%and the rest are found in a list of key-value pairs
split(From, To, Store) ->
  lists:splitwith(fun({Key,_}) -> key:between(From, To, Key) end, Store).

%Add a list of key-value pairs to a store.
merge(Entries, Store) ->
  lists:foldl(fun({Key, Value}, AccIn) -> add(Key, Value, AccIn) end , Store, Entries).