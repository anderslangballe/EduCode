package com.educode.nodes.method;

import com.educode.IReferencing;
import com.educode.nodes.base.BinaryNode;
import com.educode.nodes.base.ListNode;
import com.educode.nodes.base.Node;
import com.educode.nodes.referencing.IReference;
import com.educode.nodes.ungrouped.BlockNode;
import com.educode.types.Type;
import com.educode.visitors.VisitorBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zen on 3/9/17.
 */
public class MethodDeclarationNode extends BinaryNode implements IReferencing
{
    private IReference _reference;

    private int MaxDeclaredVariables;

    public void setMaxDeclaredVariables(int value) { MaxDeclaredVariables = value; }

    public int getMaxDeclaredVariables() { return MaxDeclaredVariables; }

    public MethodDeclarationNode(Node leftChild, Node rightChild, IReference reference, Type returnType)
    {
        super(leftChild, rightChild);
        this._reference = reference;
        setType(returnType);
    }

    public ArrayList<ParameterNode> getParameters()
    {
        ArrayList<ParameterNode> nodeList = new ArrayList<>();

        if (!hasParameterList())
            return nodeList; // Return an empty list

        // Add all parameter nodes from child
        for (Node grandchild : getParameterList().getChildren())
        {
            if (grandchild instanceof ParameterNode)
                nodeList.add((ParameterNode) grandchild);
        }

        return nodeList;
    }

    public ListNode getParameterList()
    {
        return (ListNode) this.getLeftChild();
    }

    public boolean hasParameterList()
    {
        return this.hasLeftChild();
    }

    public BlockNode getBlockNode()
    {
        return (BlockNode) this.getRightChild();
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        return visitor.visit(this);
    }

    @Override
    public IReference getReference()
    {
        return this._reference;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof MethodDeclarationNode))
            return false;
        else
        {
            MethodDeclarationNode otherNode = (MethodDeclarationNode) other;

            // Check if name matches
            if (!this.getReference().equals(otherNode.getReference()))
                return false;

            // Get list of parameters for each node
            List<ParameterNode> ownParameters = this.getParameters();
            List<ParameterNode> otherParameters = otherNode.getParameters();

            // Check if amount match
            if (ownParameters.size() != otherParameters.size())
                return false;

            // Amount of parameters match, now check if each parameter matches in same order
            for (int i = 0; i < ownParameters.size(); i++)
            {
                if (!ownParameters.get(i).equals(otherParameters.get(i)))
                    return false;
            }

            return true;
        }
    }

    public boolean correspondsWith(IReference reference, List<Type> actualTypes)
    {
        // Check if name matches
        if (!this.getReference().equals(reference))
            return false;

        // Get formal parameters
        List<ParameterNode> formalParameters = this.getParameters();

        // Check if amount matches
        if (formalParameters.size() != actualTypes.size())
            return false;

        // Check parameter one by one to match type
        for (int i = 0; i < formalParameters.size(); i++)
        {
            Type formalType = formalParameters.get(i).getType();
            Type actualType = actualTypes.get(i);

            if (!formalType.equals(actualType))
                return false;
        }

        return true;
    }
}
