package com.educode.visitors;

import com.educode.helper.OperatorTranslator;
import com.educode.nodes.Typeable;
import com.educode.nodes.base.ListNode;
import com.educode.nodes.expression.AdditionExpression;
import com.educode.nodes.expression.MultiplicationExpression;
import com.educode.nodes.expression.logic.*;
import com.educode.nodes.literal.BoolLiteralNode;
import com.educode.nodes.literal.IdentifierLiteralNode;
import com.educode.nodes.literal.NumberLiteralNode;
import com.educode.nodes.literal.StringLiteralNode;
import com.educode.nodes.method.MethodDeclarationNode;
import com.educode.nodes.method.MethodInvocationNode;
import com.educode.nodes.method.ParameterNode;
import com.educode.nodes.statement.AssignmentNode;
import com.educode.nodes.statement.ReturnNode;
import com.educode.nodes.statement.VariableDeclarationNode;
import com.educode.nodes.statement.conditional.ConditionNode;
import com.educode.nodes.statement.conditional.IfNode;
import com.educode.nodes.statement.conditional.RepeatWhileNode;
import com.educode.nodes.ungrouped.BlockNode;
import com.educode.nodes.ungrouped.ObjectInstantiationNode;
import com.educode.nodes.ungrouped.ProgramNode;
import com.educode.symboltable.Symbol;
import com.educode.symboltable.SymbolTableHandler;
import com.educode.types.Type;

/**
 * Created by zen on 4/5/17.
 */
public class SemanticVisitor extends VisitorBase
{
    private SymbolTableHandler _symbolTableHandler = new SymbolTableHandler();

    public SymbolTableHandler getSymbolTableHandler()
    {
        return _symbolTableHandler;
    }

    @Override
    public Object visit(ProgramNode node)
    {
        _symbolTableHandler.openScope();

        // Add method declarations to symbol table
        for (MethodDeclarationNode methodDecl : node.getMethodDeclarations())
        {
            _symbolTableHandler.enterSymbol(methodDecl);

            visit(methodDecl);
        }

        _symbolTableHandler.closeScope();

        return null;
    }

    @Override
    public Object visit(BlockNode node)
    {
        _symbolTableHandler.openScope();

        visitChildren(node);

        _symbolTableHandler.closeScope();

        return null;
    }

    @Override
    public Object visit(ListNode node)
    {
        visitChildren(node);

        return null;
    }

    @Override
    public Object visit(ObjectInstantiationNode node)
    {
        return null;
    }

    @Override
    public Object visit(MethodDeclarationNode node)
    {
        _symbolTableHandler.openScope();

        // Visit parameters
        if (node.getParameterList() != null)
            visitChildren(node.getParameterList());

        // Visit block
        visit(node.getBlockNode());

        _symbolTableHandler.closeScope();

        return null;
    }

    @Override
    public Object visit(MethodInvocationNode node)
    {
        return null;
    }

    @Override
    public Object visit(ParameterNode node)
    {
        _symbolTableHandler.enterSymbol(node);

        return null;
    }

    @Override
    public Object visit(AssignmentNode node)
    {
        visitChildren(node);

        // Get the symbol that corresponds to the left side
        Symbol leftSide = _symbolTableHandler.retreiveSymbol(node.getIdentifierNode());
        if (leftSide == null)
            _symbolTableHandler.error(node, String.format("Identifier %s is not declared.", node.getIdentifier()));
        else
        {
            // Check if right side has a type
            if (node.hasChild() && node.getChild() instanceof Typeable)
            {
                Type leftSideType = ((Typeable) leftSide.getNode()).getType(); // todo: Not a good way of doing this..
                Type rightSideType = ((Typeable) node.getChild()).getType();

                // If type is void, it can not be assigned to anything
                // Otherwise check if type of sides are equal - they must be
                if (leftSideType.equals(Type.VoidType))
                    _symbolTableHandler.error(node, String.format("%s is of type %s, it can not be assigned.", node.getIdentifier(), OperatorTranslator.toString(leftSideType)));
                else if (!leftSideType.equals(rightSideType))
                    _symbolTableHandler.error(node, String.format("%s is of type %s, can not be assigned to %s.", node.getIdentifier(), OperatorTranslator.toString(leftSideType), OperatorTranslator.toString(rightSideType)));

            }
            else
                _symbolTableHandler.error(node, "Right side of the assignment does not have a type.");
        }

        return null;
    }

    @Override
    public Object visit(VariableDeclarationNode node)
    {
        _symbolTableHandler.enterSymbol(node);

        visitChildren(node);

        return null;
    }

    @Override
    public Object visit(IfNode node)
    {
        return null;
    }

    @Override
    public Object visit(ConditionNode node)
    {
        return null;
    }

    @Override
    public Object visit(RepeatWhileNode node)
    {
        return null;
    }

    @Override
    public Object visit(ReturnNode node)
    {
        return null;
    }

    @Override
    public Object visit(MultiplicationExpression node)
    {
        Type leftType = ((Typeable)node.getLeftChild()).getType();
        Type rightType = ((Typeable)node.getLeftChild()).getType();

        if ((leftType.Kind == Type.NUMBER) ){

        }


        return null;
    }

    @Override
    public Object visit(AdditionExpression node)
    {
        return null;
    }

    @Override
    public Object visit(NumberLiteralNode node)
    {
        return null;
    }

    @Override
    public Object visit(StringLiteralNode node)
    {
        return null;
    }

    @Override
    public Object visit(IdentifierLiteralNode node)
    {
        Symbol s = _symbolTableHandler.retreiveSymbol(node);
        if (s != null)
            if (s.getNode() instanceof Typeable)
                node.setType(((Typeable)s.getNode()).getType());
            else
                _symbolTableHandler.error(node, "Invalid variable type."); //Should not happen.
        else
            _symbolTableHandler.error(node, String.format("Variable %s has not been declared.", node.getIdentifier()));

        return null;
    }

    @Override
    public Object visit(BoolLiteralNode node)
    {
        return null;
    }

    @Override
    public Object visit(OrExpressionNode node)
    {
        return null;
    }

    @Override
    public Object visit(AndExpressionNode node)
    {
        return null;
    }

    @Override
    public Object visit(RelativeExpressionNode node)
    {
        return null;
    }

    @Override
    public Object visit(EqualExpressionNode node)
    {


        return null;
    }

    @Override
    public Object visit(NegateNode node)
    {
        visitChildren(node);

        if (node.getChild() instanceof Typeable)
        {
            if(((Typeable)node.getChild()).getType().Kind == Type.BOOL)
                _symbolTableHandler.error(node, "Negated expression was not of boolean type.");
        }
        else
            _symbolTableHandler.error(node, "Negated expression did not have a type."); // should not happen..

        return null;
    }
}