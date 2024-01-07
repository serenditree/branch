package com.serenditree.root.etc.maple;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.InternalServerErrorException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class providing various mapping routines.
 */
public class Maple {

    private Maple() {
    }

    private static final Logger LOGGER = Logger.getLogger(Maple.class.getName());

    private static final Jsonb JSONB = JsonbBuilder.create();
    private static final Jsonb PRETTY_JSONB = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    /**
     * Maps the items of a list to the desired type and puts them into the desired implementation of List.
     *
     * @param from    Source list
     * @param mapItem Function for mapping F to T
     * @param to      New instance of the desired target implementation
     * @param <F>     From type
     * @param <T>     Target type
     * @return List with items of type T
     */
    public static <F, T> List<T> mapList(List<F> from, Function<F, T> mapItem, List<T> to) {

        if (from != null) {
            to = from
                .stream()
                .map(mapItem)
                .collect(Collectors.toList());
        }

        return to;
    }

    /**
     * Overloaded {@link #mapList(List, Function, List) mapList} where the target list defaults to ArrayList.
     *
     * @param from    Source list
     * @param mapItem Function for mapping F to T
     * @param <F>     From type
     * @param <T>     Target type
     * @return ArrayList with items of type T
     * @see #mapList(List, Function, List)
     */
    public static <F, T> List<T> mapList(List<F> from, Function<F, T> mapItem) {

        return Maple.mapList(from, mapItem, new ArrayList<>());
    }

    /**
     * Maps the items of a Set to the desired type and puts them into the desired implementation of Set.
     *
     * @param from    Source set
     * @param mapItem Function for mapping F to T
     * @param to      New instance of the desired target implementation
     * @param <F>     From type
     * @param <T>     Target type
     * @return Set
     */
    public static <F, T> Set<T> mapSet(Set<F> from, Function<F, T> mapItem, Set<T> to) {

        if (from != null) {
            to = from
                .stream()
                .map(mapItem)
                .collect(Collectors.toSet());
        }

        return to;
    }

    /**
     * Overloaded {@link #mapSet(Set, Function, Set) mapSet} where the target set defaults to HashSet.
     *
     * @param from    Source set
     * @param mapItem Function for mapping F to T
     * @param <F>     From type
     * @param <T>     Target type
     * @return Set
     * @see #mapSet(Set, Function, Set)
     */
    public static <F, T> Set<T> mapSet(Set<F> from, Function<F, T> mapItem) {

        return Maple.mapSet(from, mapItem, new HashSet<>());
    }

    /**
     * Maps the items of a Set to the desired type and puts them into an ArrayList.
     *
     * @param from    Source set
     * @param mapItem Function for mapping F to T
     * @param <F>     From type
     * @param <T>     Target type
     * @return ArrayList
     */
    public static <F, T> List<T> mapSetToList(Set<F> from, Function<F, T> mapItem) {

        List<T> to = new ArrayList<>();

        if (from != null) {
            to = from
                .stream()
                .map(mapItem)
                .collect(Collectors.toList());
        }

        return to;
    }

    /**
     * Maps an exception to its root cause.
     *
     * @param throwable Exception which might wrap a root cause.
     * @return Root cause of the exception.
     */
    public static Throwable toRootCause(Throwable throwable) {
        Throwable cause;
        Throwable result = throwable;

        LOGGER.fine("Root cause for: " + throwable.getClass().getName());
        while ((cause = result.getCause()) != null && result != cause) {
            LOGGER.fine(cause.getClass().getName());
            result = cause;
        }
        LOGGER.fine("Root cause: " + result.getMessage());

        return result;
    }

    /**
     * Maps an exception to its causal chain.
     *
     * @param throwable Exception which might wrap a causal chain.
     * @return Causal chain of the exception.
     */
    public static List<Class<?>> toCausalChain(Throwable throwable) {
        Throwable cause;
        Throwable result = throwable;
        List<Class<?>> causalChain = new ArrayList<>();
        causalChain.add(throwable.getClass());

        while ((cause = result.getCause()) != null && result != cause) {
            causalChain.add(cause.getClass());
            result = cause;
        }

        return causalChain;
    }

    /**
     * Pretty prints objects in JSON style.
     *
     * @param object Object to print
     * @return Pretty JSON String.
     */
    public static String prettyJson(Object object) {

        return Maple.json(object, true);
    }

    /**
     * Prints objects in JSON style using an instance of Jsonb.
     *
     * @param object Object to print
     * @return JSON String.
     */
    public static String json(Object object) {

        return Maple.json(object, false);
    }

    /**
     * Prints objects in JSON style using an instance of Jsonb.
     *
     * @param object Object to print
     * @param pretty pretty print or not
     * @return JSON String.
     */
    private static String json(Object object, boolean pretty) {

        String json = null;

        if (object != null) {
            try {
                if (pretty) {
                    json = Maple.PRETTY_JSONB.toJson(object);
                } else {
                    json = Maple.JSONB.toJson(object);
                }
            } catch (JsonbException e) {
                json = object.toString();
                LOGGER.warning(() ->
                                   "Could not map object to json. Object::toString method is used instead: " +
                                   e.getMessage()
                );
            }
        }

        return json;
    }


    /**
     * Maps the given JSON string to an object of type T.
     *
     * @param json JSON string
     * @param type Target type
     * @param <T>  Target type
     * @return Object of target type
     */
    public static <T> T fromJson(String json, Class<T> type) {

        T object;

        try {
            object = Maple.JSONB.fromJson(json, type);
        } catch (JsonbException e) {
            String message = "Could not map json to an object: " + e.getMessage();
            LOGGER.severe(message);
            throw new InternalServerErrorException(message, e);
        }

        return object;
    }
}
