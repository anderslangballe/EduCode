package com.educode.nodes.base;

import com.educode.visitors.VisitorBase;

import java.util.ArrayList;

/**
 * Created by zen on 3/23/17.
 */
public class CollectionNode extends NaryNode
{
    public CollectionNode()
    {
        super(new ArrayList<NodeBase>());
    }

    public CollectionNode(ArrayList<NodeBase> childNodes)
    {
        super(childNodes);
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        return visitor.visit(this);
    }
}