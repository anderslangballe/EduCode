package com.educode.symboltable;

import com.educode.nodes.Identifiable;
import com.educode.nodes.base.Node;
import com.educode.nodes.method.MethodDeclarationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Buhl on 31/03/2017.
 */
public class SymbolTableHandler
{
    public SymbolTable current = new SymbolTable(); // todo make getter/setter
    private List<SymbolTableMessage> _messageList = new ArrayList<>();
    private MethodDeclarationNode _currentParent;

    public void openScope()
    {
        current = new SymbolTable(current);
    }

    public void closeScope()
    {
        if(current.outer != null)
            current = current.outer;
        else
            error("Can not close scope when not within a scope.");
    }

    public void enterSymbol(Identifiable node)
    {
        boolean isNew = !current.contains(node);

        current.symbolList.add(new Symbol(node, isNew));

        // Add an error for multiple declaration
        if (!isNew)
            error((Node) node, String.format("Identifier %s already declared.", node.getIdentifier()));
        // todo: Should not be identifiable in parameter, we need to know line number which is contained in node
    }

    public Symbol retreiveSymbol(Node node)
    {
        return current.getSymbol(node);
    }

    //needs modification
    public boolean declaredLocally(Node node)
    {
        for (Symbol s : current.symbolList)
        {
            if (s.equals(node))
                return true;
        }

        return false;
    }

    public void printMessages()
    {
        for (SymbolTableMessage message : _messageList)
            System.out.println(message);
    }

    private void error(String description)
    {
        error(null, description);
    }

    public void error(Node relatedNode, String description)
    {
        this._messageList.add(new SymbolTableMessage(SymbolTableMessage.MessageType.ERROR, relatedNode, description));
    }

    public void warning(Node relatedNode, String description)
    {
        this._messageList.add(new SymbolTableMessage(SymbolTableMessage.MessageType.WARNING, relatedNode, description));
    }

    public void setCurrentParent(MethodDeclarationNode currentParent)
    {
        this._currentParent = currentParent;
    }

    public MethodDeclarationNode getCurrentParent()
    {
        return this._currentParent;
    }

    public boolean hasErrors()
    {
        for (SymbolTableMessage message : _messageList)
        {
            if (message.getType() == SymbolTableMessage.MessageType.ERROR)
                return true;
        }

        return false;
    }
}