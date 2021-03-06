package com.educode.visitors.codegeneration;

import com.educode.helper.OperatorTranslator;
import com.educode.minecraft.CompilerMod;
import com.educode.nodes.ISingleLineStatement;
import com.educode.nodes.base.ListNode;
import com.educode.nodes.base.NaryNode;
import com.educode.nodes.base.Node;
import com.educode.nodes.expression.AdditionExpressionNode;
import com.educode.nodes.expression.MultiplicationExpressionNode;
import com.educode.nodes.expression.RangeNode;
import com.educode.nodes.expression.UnaryMinusNode;
import com.educode.nodes.expression.logic.*;
import com.educode.nodes.literal.*;
import com.educode.nodes.method.MethodDeclarationNode;
import com.educode.nodes.method.MethodInvocationNode;
import com.educode.nodes.method.ParameterNode;
import com.educode.nodes.referencing.ArrayReferencingNode;
import com.educode.nodes.referencing.IdentifierReferencingNode;
import com.educode.nodes.referencing.StructReferencingNode;
import com.educode.nodes.statement.*;
import com.educode.nodes.statement.conditional.ConditionNode;
import com.educode.nodes.statement.conditional.IfNode;
import com.educode.nodes.statement.conditional.RepeatWhileNode;
import com.educode.nodes.ungrouped.*;
import com.educode.runtime.annotations.SpecialJavaTranslation;
import com.educode.types.ArithmeticOperator;
import com.educode.types.LogicalOperator;
import com.educode.types.Type;
import com.educode.visitors.VisitorBase;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Andreas on 10-04-2017.
 */
public class JavaCodeGenerationVisitor extends VisitorBase
{
    private void append(StringBuffer buffer, String format, Object... args)
    {
        try
        {
            buffer.append(String.format(format, args));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Object defaultVisit(Node node)
    {
        System.out.println("Please implement in Java CodeGen:" + node.getClass().getName());

        return "NOT IMPLEMENTED:" + node.getClass().getName();
    }

    public void visit(StartNode node)
    {
        visit(node.getRightChild());
    }

    public void visit(ProgramNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();

        append(codeBuffer, "import java.util.*;\nimport com.educode.runtime.*;\nimport com.educode.runtime.types.*;\nimport com.educode.helper.*;\n\n");
        append(codeBuffer, "public class %s extends ProgramBase\n{\n", node.getReference());

        // Visit global variable declarations
        for (VariableDeclarationNode variableDecl : node.getVariableDeclarations())
            append(codeBuffer, "%s;\n", visit(variableDecl));

        // Visit method declarations
        for (MethodDeclarationNode methodDecl : node.getMethodDeclarations())
            append(codeBuffer, "%s", visit(methodDecl));

        // Append closing curly bracket
        append(codeBuffer, "}");

        // Write codeBuffer to file
        try
        {
            FileWriter fw = new FileWriter(CompilerMod.EDUCODE_PROGRAMS_LOCATION + node.getReference() + ".java");
            fw.append(codeBuffer);
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Object visit(BlockNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();

        append(codeBuffer, "{\n");

        // Visit statements in body/block declarations
        for (Node childNode : node.getChildren())
        {
            //this if statement is here in order to ensure that if statements does not have semicolon
            if (childNode instanceof ISingleLineStatement)
                append(codeBuffer, "%s;\n", visit(childNode));
            else
                append(codeBuffer, "%s\n", visit(childNode));
        }

        append(codeBuffer, "}\n");

        return codeBuffer;
    }

    public Object visit(ObjectInstantiationNode node)
    {
        // Join actual arguments
        StringJoiner argumentJoiner = new StringJoiner(", ");
        if (node.hasChild())
        {
            for (Node grandchild : ((NaryNode)node.getChild()).getChildren())
                argumentJoiner.add(visit(grandchild).toString());
        }

        // Object instantiation is handled differently for different types
        // This case is only used if collection is initialized with values
        return String.format("new %s(%s)", OperatorTranslator.toJava(node.getType()), argumentJoiner);
    }

    public Object visit(MethodDeclarationNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();

        // Visit parameters, join using string joiner
        StringJoiner parameterJoiner = new StringJoiner(",");
        for (ParameterNode parameterNodeDecl : node.getParameters())
            parameterJoiner.add(visit(parameterNodeDecl).toString());

        // Add declaration with joined parameters
        // All method calls can be interrupted at any time
        append(codeBuffer, String.format("public %s %s(%s) throws InterruptedException\n", OperatorTranslator.toJava(node.getType()), visit(node.getReference()), parameterJoiner));

        // Append block
        append(codeBuffer, "%s", visit(node.getBlockNode()));

        return codeBuffer;
    }

    public Object visit(MethodInvocationNode node)
    {
        StringJoiner actualArgumentsJoiner = new StringJoiner(", ");
        if (node.hasChild())
        {
            for (Node parameterNodeDecl : ((ListNode) node.getChild()).getChildren())
                actualArgumentsJoiner.add(visit(parameterNodeDecl).toString());
        }

        // If identifier has a special translation, use that instead of the method identifier
        if (node.getReferencingDeclaration().hasSpecialJavaTranslation())
        {
            SpecialJavaTranslation specialJavaTranslation = node.getReferencingDeclaration().getSpecialJavaTranslation();

            // If method invocation references a struct, we will need to pass the left side of the struct to the special translation
            if (node.getReference() instanceof StructReferencingNode)
            {
                // Create new argument joiner which contains left side of struct reference
                StringJoiner newArgJoiner = new StringJoiner(", ");
                newArgJoiner.add(visit(((StructReferencingNode) node.getReference()).getLeftChild()).toString());
                newArgJoiner.merge(actualArgumentsJoiner);

                // Return formatted string with new arguments
                return String.format(specialJavaTranslation.formattedTranslation(), newArgJoiner);
            }

            // Otherwise just return formatted string with actual arguments, since it is not a struct reference
            return String.format(specialJavaTranslation.formattedTranslation(), actualArgumentsJoiner);
        }

        return String.format("%s(%s)", visit(node.getReference()), actualArgumentsJoiner);
    }

    public Object visit(ParameterNode node)
    {
        //formal parameter node
        StringBuffer codeBuffer = new StringBuffer();
        append(codeBuffer, "%s %s", OperatorTranslator.toJava(node.getType()), visit(node.getReference()));

        return codeBuffer;
    }

    public Object visit(AssignmentNode node)
    {
        // Special case for array references.
        if (node.getReference() instanceof ArrayReferencingNode)
        {
            ArrayReferencingNode arrayReference = (ArrayReferencingNode) node.getReference();
            return String.format("%s.setItemAt(%s, %s)", visit(arrayReference.getLeftChild()), visit(arrayReference.getRightChild()), visit(node.getChild()));
        }

        return String.format("%s = %s",  visit(node.getReference()), visit(node.getChild()));
    }

    public Object visit(VariableDeclarationNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();
        append(codeBuffer, "%s ", OperatorTranslator.toJava(node.getType()));

        if (node.getChild() != null)
            append(codeBuffer, "%s", visit(node.getChild()));
        else
            append(codeBuffer, "%s", visit(node.getReference()));

        return codeBuffer;
    }


    public Object visit(IfNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();
        ArrayList<ConditionNode> conditions = node.getConditionBlocks();

        // visit if and else if
        int i = 0;
        for (ConditionNode condition : conditions)
        {
            if (i++ == 0) //visit if
                append(codeBuffer, "if%s", visit(condition));
            else //visit else if
                append(codeBuffer, "else if%s", visit(condition));
        }

        // Visit else block (if any)
        BlockNode elseBlock = node.getElseBlock();
        if (elseBlock != null)
            append(codeBuffer, "else\n%s", visit(elseBlock));

        return codeBuffer;
    }

    public Object visit(ConditionNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();
        append(codeBuffer, "(%s)\n%s", visit(node.getLeftChild()), visit(node.getRightChild()));
        return codeBuffer;
    }

    public Object visit(RepeatWhileNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();
        append(codeBuffer, "while %s", visit(node.getChild()));

        return codeBuffer;
    }

    public Object visit(ForEachNode node)
    {
        return String.format("for (%s %s : %s)\n%s", OperatorTranslator.toJava(node.getType()), visit(node.getReference()), visit(node.getLeftChild()), visit(node.getRightChild()));
    }

    public Object visit(ReturnNode node)
    {
        if (node.hasChild())
            return String.format("return %s", visit(node.getChild()));
        else
            return "return";
    }

    public Object visit(BreakNode node)
    {
        return "break";
    }

    public Object visit(ContinueNode node)
    {
        return "continue";
    }

    public Object visit(MultiplicationExpressionNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();
        append(codeBuffer, "(%s %s %s)", visit(node.getLeftChild()), OperatorTranslator.toJava(node.getOperator()), visit(node.getRightChild()));

        return codeBuffer;
    }

    public Object visit(AdditionExpressionNode node)
    {
        boolean coordinateAddition = node.getLeftChild().isType(Type.CoordinatesType) && node.getRightChild().isType(Type.CoordinatesType);

        if (coordinateAddition)
            return String.format("%s.add(%s, %s)", visit(node.getLeftChild()), visit(node.getRightChild()), node.getOperator().equals(ArithmeticOperator.Subtraction));
        else
            return String.format("(%s %s %s)", visit(node.getLeftChild()), OperatorTranslator.toJava(node.getOperator()), visit(node.getRightChild()));
    }

    public Object visit(NumberLiteralNode node)
    {
        return node.getValue().toString();
    }

    public Object visit(StringLiteralNode node)
    {
        return node.getValue();
    }

    public Object visit(BoolLiteralNode node)
    {
        return node.getValue();
    }

    public Object visit(NullLiteralNode node)
    {
        return node.getValue();
    }

    public Object visit(CoordinatesLiteralNode node)
    {
        // Uses the Coordinates runtime type
        return String.format("new Coordinates(%s, %s, %s)", visit(node.getX()), visit(node.getY()), visit(node.getZ()));
    }

    public Object visit(OrExpressionNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();
        append(codeBuffer, "(%s %s %s)", visit(node.getLeftChild()), OperatorTranslator.toJava(node.getOperator()), visit(node.getRightChild()));

        return codeBuffer;
    }

    public Object visit(AndExpressionNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();
        append(codeBuffer, "(%s %s %s)", visit(node.getLeftChild()), OperatorTranslator.toJava(node.getOperator()), visit(node.getRightChild()));

        return codeBuffer;
    }

    public Object visit(RangeNode node)
    {
        return String.format("range(%s, %s)", visit(node.getLeftChild()), visit(node.getRightChild()));
    }

    public Object visit(RelativeExpressionNode node)
    {
        StringBuffer codeBuffer = new StringBuffer();

        Type leftType  = node.getLeftChild().getType();
        Type rightType = node.getRightChild().getType();

        if (leftType.equals(Type.StringType) && rightType.equals(Type.StringType))
            append(codeBuffer, "(%s.compareTo(%s) %s 0)", visit(node.getLeftChild()), visit(node.getRightChild()), OperatorTranslator.toJava(node.getOperator()));
        else
            append(codeBuffer, "(%s %s %s)", visit(node.getLeftChild()), OperatorTranslator.toJava(node.getOperator()), visit(node.getRightChild()));

        return codeBuffer;
    }

    public Object visit(EqualExpressionNode node)
    {
        // In case of a string comparison, we need to use equals()
        // In theory we only need to check either the left or right type, because the semantic visitor only allows equal comparison of equal types
        boolean useEqualSymbol = node.isBoolComparison() || node.isNumberComparison() || node.isNullComparison();

        if (useEqualSymbol)
            return String.format("(%s %s %s)", visit(node.getLeftChild()), OperatorTranslator.toJava(node.getOperator()), visit(node.getRightChild()));
        else
        {
            String returnString = String.format("%s.equals(%s)", visit(node.getLeftChild()), visit(node.getRightChild()));

            // Return code, negate if operator is NOT EQUALS
            if (node.getOperator().equals(LogicalOperator.NotEquals))
                return String.format("!%s", returnString);
            else
                return returnString;
        }
    }

    public Object visit(NegateNode node)
    {
        return String.format("!(%s)", visit(node.getChild()));
    }

    public Object visit(UnaryMinusNode node)
    {
        if (node.getType().equals(Type.CoordinatesType))
            return String.format("%s.negate()", visit(node.getChild()));
        else
            return String.format("-(%s)", visit(node.getChild()));
    }

    public Object visit(TypeCastNode node)
    {
        return String.format("(%s)(%s)", OperatorTranslator.toJava(node.getType()), visit(node.getChild()));
    }

    public Object visit(IdentifierReferencingNode node)
    {
        return node.getText();
    }

    public Object visit(ArrayReferencingNode node)
    {
        return String.format("%s.getItemAt(%s)", visit(node.getArrayName()), visit(node.getExpression()));
    }

    public Object visit(StructReferencingNode node)
    {
        if (node.getRightChild() instanceof MethodInvocationNode)
        {
            MethodInvocationNode methodInvocation = (MethodInvocationNode) node.getRightChild();
            MethodDeclarationNode methodDeclaration = methodInvocation.getReferencingDeclaration();

            if (methodDeclaration.hasSpecialJavaTranslation())
            {
                StringJoiner argumentJoiner = new StringJoiner(", ");
                argumentJoiner.add(visit(node.getObjectName()).toString());

                List<Node> actualArguments = methodInvocation.getActualArguments();
                for (Node argument : actualArguments)
                    argumentJoiner.add(visit(argument).toString());

                return String.format(methodDeclaration.getSpecialJavaTranslation().formattedTranslation(), argumentJoiner);
            }
        }
        return String.format("%s.%s", visit(node.getObjectName()), visit(node.getMemberName()));
    }
}
