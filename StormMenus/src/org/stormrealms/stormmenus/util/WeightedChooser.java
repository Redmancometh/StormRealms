package org.stormrealms.stormmenus.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedChooser<T> extends ConcurrentHashMap<Pair<Double, Double>, T>
{
    private static final long serialVersionUID = 1L;
    private double upperBound = 0;

    public T getRandomElement()
    {
        double index = ThreadLocalRandom.current().nextDouble(0, upperBound);
        for (Pair<Double, Double> range : this.keySet())
        {
            if (range.getKey() < index && range.getValue() > index)
            {
                return this.get(range);
            }
        }
        return null;
    }

    public void insertElement(T e, double probability)
    {
        double upperBound = getUpperBound();
        this.put(new Pair(upperBound, upperBound + probability), e);
        this.upperBound = upperBound + probability;
    }

    public double getUpperBound()
    {
        return upperBound;
    }
}
