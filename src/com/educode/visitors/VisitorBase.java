package com.educode.visitors;

import com.educode.nodes.expression.logic.*;
import com.educode.nodes.literal.BoolLiteralNode;
import com.educode.nodes.literal.IdentifierLiteralNode;
import com.educode.nodes.literal.NumberLiteralNode;
import com.educode.nodes.literal.StringLiteralNode;
import com.educode.nodes.method.MethodInvokationNode;
import com.educode.nodes.statement.conditional.ConditionNode;
import com.educode.nodes.statement.conditional.IfNode;
import com.educode.nodes.statement.conditional.RepeatWhileNode;
import com.educode.nodes.ungrouped.BlockNode;
import com.educode.nodes.ungrouped.ProgramNode;
import com.educode.nodes.base.*;
import com.educode.nodes.expression.AdditionExpression;
import com.educode.nodes.expression.MultiplicationExpression;
import com.educode.nodes.method.MethodDeclarationNode;
import com.educode.nodes.statement.AssignmentNode;
import com.educode.nodes.statement.VariableDeclarationNode;

/**
 * Created by zen on 3/23/17.
 */
public abstract class VisitorBase
{
    public abstract Object visit(ProgramNode node);
    public abstract Object visit(BlockNode node);
    public abstract Object visit(CollectionNode node);

    // Methods
    public abstract Object visit(MethodDeclarationNode node);
    public abstract Object visit(MethodInvokationNode node);

    // Statements
    public abstract Object visit(AssignmentNode node);
    public abstract Object visit(VariableDeclarationNode node);
    public abstract Object visit(IfNode node);
    public abstract Object visit(ConditionNode node);
    public abstract Object visit(RepeatWhileNode node);

    // Arithmetic
    public abstract Object visit(MultiplicationExpression node);
    public abstract Object visit(AdditionExpression node);

    // Literals
    public abstract Object visit(NumberLiteralNode node);
    public abstract Object visit(StringLiteralNode node);
    public abstract Object visit(IdentifierLiteralNode node);
    public abstract Object visit(BoolLiteralNode node);

    // Logical
    public abstract Object visit(OrExpressionNode node);
    public abstract Object visit(AndExpressionNode node);
    public abstract Object visit(RelativeExpressionNode node);
    public abstract Object visit(EqualExpressionNode node);
    public abstract Object visit(NegateNode node);

    public Object visit(NodeBase node)
    {
        // System.out.println(node.getClass().getName());
        return node.accept(this);
    }

    public void visitChild(UnaryNode node)
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
        for (NodeBase child : node.getChildren())
        {
            child.accept(this);
        }
    }
}