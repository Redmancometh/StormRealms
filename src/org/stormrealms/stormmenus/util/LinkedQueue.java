package org.stormrealms.stormmenus.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

class Itr<E> implements Iterator
{

    private boolean first = true;
    private QueueNode<E> itr;

    public Itr(QueueNode<E> first)
    {
        this.itr = first;
    }

    @Override
    public boolean hasNext()
    {
        return this.itr.getNextNode() != null;
    }

    @Override
    public E next()
    {
        if (this.first)
        {
            this.first = false;
        } else if (this.hasNext())
        {
            this.itr = this.itr.getNextNode();
        }
        return this.itr.getValue();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Cannot remove content from iterator");
    }
}

public final class LinkedQueue<E> implements Collection<E>
{

    private QueueNode<E> loc;
    private QueueNode<E> node;
    private int size = 0;

    public LinkedQueue(E... inits)
    {
        this.addAll(Arrays.asList(inits));
    }

    public LinkedQueue()
    {
        this.node = new QueueNode<>(null, null, null);
    }

    public E dequeue()
    {
        if (this.size == 0)
        {
            throw new IllegalStateException("LinkedQueue is empty!");
        } else
        {
            E val = this.node.getValue();
            this.remove(val);
            this.size--;
            return val;
        }
    }

    public E getFront()
    {
        if (this.size == 0)
        {
            throw new IllegalStateException("LinkedQueue is empty!");
        } else
        {
            return this.node.getValue();
        }
    }

    public E getNext()
    {
        if (this.loc == null)
        {
            this.loc = this.node;
        } else
        {
            this.loc = this.loc.getNextNode();
        }
        if (this.loc == null)
        {
            this.loc = this.node;
        }
        return this.loc.getValue();
    }

    public int getSize()
    {
        return this.size;
    }

    public boolean insertBefore(E itemToInsert, E insertBefore)
    {
        QueueNode<E> temp = this.node;
        while (temp.getNextNode() != null)
        {
            if (temp.getValue() == insertBefore)
            {
                QueueNode<E> insert = new QueueNode<>(temp.getPreviousNode(), itemToInsert, temp);
                if (temp.getPreviousNode() != null)
                {
                    temp.getPreviousNode().setNextNode(insert);
                }
                temp.setPreviousNode(insert);
                this.size++;
                return true;
            } else
            {
                temp = temp.getNextNode();
            }
        }
        return false;
    }

    @Override
    public int size()
    {
        return this.size;
    }

    public boolean isEmpty()
    {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o)
    {
        QueueNode<E> next = this.node;
        while (next != null)
        {
            if (next.getValue() == o)
            {
                return true;
            }
            next = next.getNextNode();
        }
        return false;
    }

    @Override
    public Iterator iterator()
    {
        return new Itr(this.node);
    }

    @Override
    public Object[] toArray()
    {
        Object[] back = new Object[this.size];
        return back;
    }

    @SuppressWarnings("unused")
    @Override
    public <T> T[] toArray(T[] a)
    {
        QueueNode<E> next = this.node;
        try
        {
            return (T[]) this.toArray();
        } catch (ClassCastException e)
        {
            return null;
        }
    }

    @Override
    public boolean add(E newElement)
    {
        if (size == 0)
        {
            node = new QueueNode<>(null, newElement, null);
            this.size++;
            return true;
        } else
        {
            QueueNode<E> temp = this.node;
            while (temp.getNextNode() != null)
            {
                if (temp.getNextNode() != null)
                {
                    temp = temp.getNextNode();
                }
            }
            temp.setNextNode(new QueueNode<>(this.node, newElement, null));
            this.size++;
            return true;
        }
    }

    @Override
    public boolean remove(Object itemToRemove)
    {
        QueueNode<E> temp = this.node;
        while (temp.getNextNode() != null)
        {
            if (temp.getValue() == itemToRemove)
            {
                if (temp.getPreviousNode() == null)
                {
                    temp.getNextNode().setPreviousNode(null);
                    this.node = temp.getNextNode();
                } else if (temp.getNextNode() == null)
                {
                    temp.getPreviousNode().setNextNode(null);
                } else
                {
                    temp.getPreviousNode().setNextNode(temp.getNextNode());
                    temp.getNextNode().setPreviousNode(temp.getPreviousNode());
                }
                this.size--;
                return true;
            } else
            {
                temp = temp.getNextNode();
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        boolean all = true;
        for (Object o : c)
        {
            if (!this.contains(o))
            {
                all = false;
            }
        }
        return all;
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        boolean all = true;
        for (E o : c)
        {
            this.add(o);
        }
        return all;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean all = true;
        for (Object o : c)
        {
            try
            {
                this.remove(o);
            } catch (ClassCastException e)
            {
                all = false;
            }
        }
        return all;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean all = true;
        LinkedQueue<E> nodes = new LinkedQueue();
        for (Object o : this)
        {
            if (!c.contains(o))
            {
                try
                {
                    nodes.add((E) o);
                } catch (ClassCastException e)
                {
                    all = false;
                }
            }
        }
        this.removeAll(nodes);
        return all;
    }

    @Override
    public void clear()
    {
        this.node = new QueueNode<>(null, null, null);
        this.size = 0;
    }
}

class QueueNode<E>
{

    private QueueNode<E> next;
    private QueueNode<E> previous;
    private E value;

    public QueueNode(QueueNode<E> previous, E value, QueueNode<E> next)
    {
        this.previous = previous;
        this.next = next;
        this.value = value;
    }

    public QueueNode<E> getNextNode()
    {
        return this.next;
    }

    public void setNextNode(QueueNode<E> next)
    {
        this.next = next;
    }

    public QueueNode<E> getPreviousNode()
    {
        return this.previous;
    }

    public void setPreviousNode(QueueNode<E> previous)
    {
        this.previous = previous;
    }

    public E getValue()
    {
        return this.value;
    }

    public void setValue(E value)
    {
        this.value = value;
    }
}