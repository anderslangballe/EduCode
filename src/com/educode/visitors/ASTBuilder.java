package com.educode.visitors;

import com.educode.antlr.EduCodeBaseVisitor;
import com.educode.antlr.EduCodeParser;
import com.educode.nodes.Identifiable;
import com.educode.nodes.base.CollectionNode;
import com.educode.nodes.base.NodeBase;
import com.educode.nodes.expression.AdditionExpression;
import com.educode.nodes.expression.MultiplicationExpression;
import com.educode.nodes.expression.logic.AndExpressionNode;
import com.educode.nodes.expression.logic.EqualExpressionNode;
import com.educode.nodes.expression.logic.NegateNode;
import com.educode.nodes.expression.logic.OrExpressionNode;
import com.educode.nodes.literal.BoolLiteralNode;
import com.educode.nodes.literal.IdentifierLiteralNode;
import com.educode.nodes.literal.NumberLiteralNode;
import com.educode.nodes.literal.StringLiteralNode;
import com.educode.nodes.method.MethodDeclarationNode;
import com.educode.nodes.method.MethodInvokationNode;
import com.educode.nodes.method.ParameterNode;
import com.educode.nodes.statement.AssignmentNode;
import com.educode.nodes.statement.ReturnNode;
import com.educode.nodes.statement.VariableDeclarationNode;
import com.educode.nodes.statement.conditional.ConditionNode;
import com.educode.nodes.statement.conditional.IfNode;
import com.educode.nodes.statement.conditional.RepeatWhileNode;
import com.educode.nodes.ungrouped.BlockNode;
import com.educode.nodes.ungrouped.ProgramNode;
import com.educode.types.ArithmeticOperator;
import com.educode.types.Type;

import java.util.ArrayList;

/**
 * Created by zen on 3/8/17.
 */
public class ASTBuilder extends EduCodeBaseVisitor<NodeBase>
{
    @Override
    public NodeBase visitParams(EduCodeParser.ParamsContext ctx)
    {
        CollectionNode parameterCollection = new CollectionNode();

        for (EduCodeParser.ParamContext p : ctx.param())
            parameterCollection.addChild(visit(p));

        return parameterCollection;
    }

    @Override
    public NodeBase visitParam(EduCodeParser.ParamContext ctx)
    {
        return new ParameterNode(ctx.IDENT().getText(), getType(ctx.dataType().getText()));
    }

    @Override
    public NodeBase visitStmt(EduCodeParser.StmtContext ctx)
    {
        return super.visitStmt(ctx); // Will pass to an appropriate statement.
    }

    @Override
    public NodeBase visitRet(EduCodeParser.RetContext ctx)
    {
        if (ctx.expr() != null)
            return new ReturnNode(visit(ctx.expr()));
        else
            return new ReturnNode();
    }

    @Override
    public NodeBase visitLoopStmt(EduCodeParser.LoopStmtContext ctx)
    {
        return new RepeatWhileNode(new ConditionNode(visit(ctx.logicExpr()), visit(ctx.stmts())));
    }

    @Override
    public NodeBase visitIfStmt(EduCodeParser.IfStmtContext ctx)
    {
        IfNode ifNode = new IfNode();

        // If there is an else block, skip the last block
        boolean hasElseBlock = ctx.logicExpr().size() < ctx.stmts().size();
        for (int i = 0; i < (hasElseBlock ? ctx.stmts().size() - 1 : ctx.stmts().size()); i++)
            ifNode.addChild(new ConditionNode(visit(ctx.logicExpr(i)), visit(ctx.stmts(i))));

        // If there is an else block, add it finally without a ConditionNode
        if (hasElseBlock)
            ifNode.addChild(visit(ctx.stmts(ctx.stmts().size() - 1)));

        return ifNode;
    }

    @Override
    public NodeBase visitExpr(EduCodeParser.ExprContext ctx)
    {
        return super.visitExpr(ctx);//visit(ctx.getChild(0));
    }

    @Override
    public NodeBase visitLogicExpr(EduCodeParser.LogicExprContext ctx)
    {
        return visit(ctx.orExpr());
    }

    @Override
    public NodeBase visitOrExpr(EduCodeParser.OrExprContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        else if (ctx.getChildCount() == 3)
            return new OrExpressionNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)));

        System.out.println("Unexpected child count in or-expression");

        return null;
    }

    @Override
    public NodeBase visitAndExpr(EduCodeParser.AndExprContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        else if (ctx.getChildCount() == 3)
            return new AndExpressionNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)));

        System.out.println("Unexpected child count in and-expression");

        return null;
    }

    @Override
    public NodeBase visitEqlExpr(EduCodeParser.EqlExprContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        else if (ctx.getChildCount() == 3)
            return new EqualExpressionNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)));

        System.out.println("Unexpected child count in equals-expression");

        return null;
    }

    @Override
    public NodeBase visitRelExpr(EduCodeParser.RelExprContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        else if (ctx.getChildCount() == 3)
            return new EqualExpressionNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)));

        System.out.println("Unexpected child count in relative-expression");

        return null;
    }

    @Override
    public NodeBase visitBoolLit(EduCodeParser.BoolLitContext ctx)
    {
        return new BoolLiteralNode(ctx.TRUE() != null);
    }

    @Override
    public NodeBase visitDataType(EduCodeParser.DataTypeContext ctx)
    {
        System.out.println("datatype");
        return null;
    }

    @Override
    public NodeBase visitEol(EduCodeParser.EolContext ctx)
    {
        return null;
    }

    @Override
    public NodeBase visitIdent(EduCodeParser.IdentContext ctx)
    {
        if (ctx.arithExpr() != null)
            return new IdentifierLiteralNode(visit(ctx.arithExpr()), ctx.getText());
        else
            return new IdentifierLiteralNode(null, ctx.getText());
    }

    @Override
    public NodeBase visitIdentName(EduCodeParser.IdentNameContext ctx)
    {
        return null;
    }

    @Override
    public NodeBase visitProgram(EduCodeParser.ProgramContext ctx)
    {
        ArrayList<NodeBase> nodes = new ArrayList<NodeBase>();
        nodes.add(visit(ctx.methods()));

        return new ProgramNode(nodes, ctx.ident().getText());
    }

    @Override
    public NodeBase visitMethods(EduCodeParser.MethodsContext ctx)
    {
        ArrayList<NodeBase> childMethods = new ArrayList<>();

        for (EduCodeParser.MethodContext m : ctx.method())
            childMethods.add(visit(m));

        return new BlockNode(childMethods);
    }

    @Override
    public NodeBase visitMethod(EduCodeParser.MethodContext ctx)
    {
        Type returnType = Type.VoidType;
        if (ctx.dataType() != null)
            returnType = getType(ctx.dataType().getText());

        return new MethodDeclarationNode(visit(ctx.stmts()), ctx.ident().getText(), returnType);
    }

    @Override
    public NodeBase visitStmts(EduCodeParser.StmtsContext ctx)
    {
        ArrayList<NodeBase> childStatements = new ArrayList<>();
        for (EduCodeParser.StmtContext s : ctx.stmt())
            childStatements.add(visit(s));
        return new BlockNode(childStatements);
    }

    @Override
    public NodeBase visitVarDcl(EduCodeParser.VarDclContext ctx)
    {
        ArrayList nodes = new ArrayList<VariableDeclarationNode>();

        // Add nodes without assignments.
        for (EduCodeParser.IdentContext i : ctx.ident())
            nodes.add(new VariableDeclarationNode(i.getText(), getType(ctx.dataType().getText()), null));

        // Add nodes with assignments
        for (EduCodeParser.AssignContext a : ctx.assign())
        {
            NodeBase assignmentNode = visit(a);
            nodes.add(new VariableDeclarationNode(((Identifiable)assignmentNode).getIdentifier(), getType(ctx.dataType().getText()), assignmentNode));
        }

        return new CollectionNode(nodes);
    }

    private ArithmeticOperator getArithmeticOperator(String operator)
    {
        switch (operator)
        {
            case "+":
                return ArithmeticOperator.AdditionOperator;
            case "-":
                return ArithmeticOperator.SubtactionOperator;
            case "/":
                return ArithmeticOperator.DivisonOperator;
            case "*":
                return ArithmeticOperator.MultiplicationOperator;
            case "%":
                return ArithmeticOperator.ModuloOperator;
            default:
                return ArithmeticOperator.Error;
        }
    }

    private Type getType(String type)
    {
        switch (type)
        {
            case "STRING":
                return Type.StringType;
            case "BOOL":
                return Type.BoolType;
            case "COORDINATES":
                return Type.CoordinatesType;
            default:
                return Type.VoidType;
        }
    }

    @Override
    public NodeBase visitAssign(EduCodeParser.AssignContext ctx)
    {
        if (ctx.getChildCount() == 3)
        {
            // Assign to arithmetic expression
            return new AssignmentNode(ctx.ident().getText(), visit(ctx.expr()));
        }

        System.out.println("Error: " + ctx.getText());

        return null;
    }

    @Override
    public NodeBase visitArithExpr(EduCodeParser.ArithExprContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.term());
        else if (ctx.getChildCount() == 3)
            return new AdditionExpression(getArithmeticOperator(ctx.ADDOP().getText()), visit(ctx.arithExpr()), visit(ctx.term()));

        System.out.println("Error: " + ctx.getText());

        return null;
    }

    @Override
    public NodeBase visitMethodC(EduCodeParser.MethodCContext ctx)
    {
        if (ctx.args() != null)
            return new MethodInvokationNode(visit(ctx.args()), ctx.ident().getText());
        else
            return new MethodInvokationNode(null, ctx.ident().getText());
    }

    @Override
    public NodeBase visitArgs(EduCodeParser.ArgsContext ctx)
    {
        CollectionNode node = new CollectionNode();
        for (EduCodeParser.ExprContext e : ctx.expr())
            node.addChild(visit(e));

        for (NodeBase n : node.getChildren())
            System.out.println(n.getClass().getName());

        return node;
    }

    @Override
    public NodeBase visitTerm(EduCodeParser.TermContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.factor());
        else if (ctx.getChildCount() == 3)
            return new MultiplicationExpression(getArithmeticOperator(ctx.MULTOP().getText()), visit(ctx.term()), visit(ctx.factor()));

        System.out.println("Error: " + ctx.getText());

        return null;
    }

    @Override
    public NodeBase visitFactor(EduCodeParser.FactorContext ctx)
    {
        if (ctx.logicExpr() != null)
            return visit(ctx.logicExpr());
        else if (ctx.literal() != null)
            return visit(ctx.literal());
        else if (ctx.methodC() != null)
            return visit(ctx.methodC());
        else if (ctx.ULOP() != null)
            return new NegateNode(visit(ctx.factor()));

        System.out.println("Error: " + ctx.getText());

        return null;
    }

    @Override
    public NodeBase visitLiteral(EduCodeParser.LiteralContext ctx)
    {
        if (ctx.NUMLIT() != null)
            return new NumberLiteralNode(Float.parseFloat(ctx.NUMLIT().getText())); // num literal
        else if (ctx.ident() != null)
            return visit(ctx.ident());
        else if (ctx.STRLIT() != null)
            return new StringLiteralNode(ctx.STRLIT().getText()); // string literal

        System.out.println("Error: " + ctx.getText());

        return null;
    }
}