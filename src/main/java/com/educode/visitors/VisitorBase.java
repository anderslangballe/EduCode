package com.educode.visitors;

import com.educode.nodes.base.BinaryNode;
import com.educode.nodes.base.NaryNode;
import com.educode.nodes.base.Node;
import com.educode.nodes.base.UnaryNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by User on 15-Apr-17.
 */
public abstract class VisitorBase
{
    public abstract Object defaultVisit(Node node);

    public Object visit(Object node)
    {
        if (node == null)
        {
            System.out.println("Attempted to invoke a null object!");
            new Exception().printStackTrace(); // Print stack trace for debugging purposes
            return null;
        }

        // Get best method for this object
        Method method = getMethodFor(node);

        // Attempt to invoke the method
        try
        {
            return method.invoke(this, node);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private Method getMethodFor(Object node)
    {
        Method ans = null;

        // Find a method which matches the class of the node
        Class currentClass = node.getClass();
        while (ans == null && currentClass != Object.class && currentClass != Node.class)
        {
            try
            {
                ans = this.getClass().getMethod("visit", currentClass);
            }
            catch (NoSuchMethodException e)
            {
                // This may happen if the implementation of the visitor does implement a visitor for all nodes
            }

            // If no appropriate method was found, look in its superclass
            if (ans == null)
                currentClass = currentClass.getSuperclass();
        }

        // If no method was found, return default method
        if (ans == null)
        {
            try
            {
                ans = this.getClass().getMethod("defaultVisit", Node.class);
            }
            catch (NoSuchMethodException e)
            {
                // Should not happen since defaultVisit is abstract and must be implemented
            }
        }

        return ans;
    }

    public void visitChildren(UnaryNode node)
    {
        if (node.getChild() != null)
            node.getChild().accept(this);
    }

    public void visitChildren(BinaryNode node)
    {
        if (node.getLeftChild() != null)
            node.getLeftChild().accept(this);
        if (node.getRightChild() != null)
            node.getRightChild().accept(this);
    }

    public void visitChildren(NaryNode node)
    {
        for (Node child : node.getChildren())
            child.accept(this);
    }
}
