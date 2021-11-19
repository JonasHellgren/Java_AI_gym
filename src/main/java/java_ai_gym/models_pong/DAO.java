package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;

import java.util.Set;

public interface DAO<T> {

    T get(String id);
    void add(String id, T item);
    void clear();
    int size();
    Set<String> keySet();



}
