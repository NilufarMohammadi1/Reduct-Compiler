package main.visitor.nameAnalyzer;

import main.ast.node.Program;
import main.ast.node.declaration.*;
import main.ast.node.declaration.handler.*;
import main.compileError.*;
import main.compileError.name.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.itemException.ItemNotFoundException;
import main.symbolTable.symbolTableItems.*;
import main.symbolTable.itemException.ItemAlreadyExistsException;
import main.symbolTable.symbolTableItems.VariableItem;
import main.visitor.Visitor;

import java.util.ArrayList;

public class NameAnalyzer extends Visitor<Void> {

    public ArrayList<CompileError> nameErrors = new ArrayList<>();

    @Override
    public Void visit(Program program) {
        SymbolTable.root = new SymbolTable();
        SymbolTable.push(SymbolTable.root);

        for (ActorDeclaration actorDeclaration : program.getActors()) {
            actorDeclaration.accept(this);
        }

        return null;
    }

    private String generateString() {
        int length = 12;
        String AlphaNumericStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";
        StringBuilder s = new StringBuilder(length);
        int i;
        for ( i=0; i<length; i++) {
            int ch = (int)(AlphaNumericStr.length() * Math.random());
            s.append(AlphaNumericStr.charAt(ch));
        }
        return s.toString();
    }


    @Override
    public Void visit(ActorDeclaration actorDeclaration) {
        //TODO make actor item and its symbol table then set it
        ActorItem item = new ActorItem(actorDeclaration);

        //TODO check the actor name is redundant or not , if it is redundant change its name and put it
        try {
            item.setName(actorDeclaration.getName().getName());
            SymbolTable.root.put(item);
        } catch (ItemAlreadyExistsException e) {
            nameErrors.add(new ActorRedefinition(actorDeclaration.getLine(), actorDeclaration.getName().getName()));
            try {
                item.setName(actorDeclaration.getName().getName() + generateString());
                SymbolTable.root.put(item);
            } catch (ItemAlreadyExistsException e_inside) {
                System.out.println("What am i doing here ?");
            }
        }

        //TODO push actor symbol table
        SymbolTable.push(new SymbolTable(SymbolTable.root, item.getKey()));


        for (VarDeclaration varDeclaration : actorDeclaration.getKnownActors()) {
            varDeclaration.accept(this);
        }

        for (VarDeclaration varDeclaration : actorDeclaration.getActorVars()) {
            varDeclaration.accept(this);
        }

        if (actorDeclaration.getInitHandler() != null) {
            actorDeclaration.getInitHandler().accept(this);
        }

        for (MsgHandlerDeclaration msgHandlerDeclaration : actorDeclaration.getMsgHandlers()) {
            msgHandlerDeclaration.accept(this);
        }

        //TODO pop actor symbol table

        item.setActorSymbolTable(SymbolTable.top);
        SymbolTable.pop();
        return null;
    }


    @Override
    public Void visit(HandlerDeclaration handlerDeclaration) {
        //TODO make handler item and its symbol table then set it
        HandlerItem item = new HandlerItem(handlerDeclaration);

        //TODO check the handler name is redundant or not , if it is redundant change its name and put it
        try {
            item.setName(handlerDeclaration.getName().getName());
            SymbolTable.top.put(item);
        }
        catch (ItemAlreadyExistsException e){
            nameErrors.add(new HandlerRedefinition(handlerDeclaration.getLine(),
                    handlerDeclaration.getName().getName()));
            try {
                item.setName(handlerDeclaration.getName() + generateString());
                SymbolTable.top.put(item);
            } catch (ItemAlreadyExistsException e_inside) {
                System.out.println("What am i doing here ?");
            }
        }


        //TODO push handler symbol table
        SymbolTable.push(new SymbolTable(SymbolTable.top, item.getKey()));

        for (VarDeclaration varDeclaration : handlerDeclaration.getArgs()) {
            varDeclaration.accept(this);
        }

        for (VarDeclaration varDeclaration : handlerDeclaration.getLocalVars()) {
            varDeclaration.accept(this);
        }

        //TODO pop handler symbol table

        item.setHandlerSymbolTable(SymbolTable.top);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VarDeclaration varDeclaration) {
        //TODO check the variable name is redundant or not
        VariableItem symbolTable = new VariableItem(varDeclaration);
        try {
            symbolTable.setName(varDeclaration.getIdentifier().getName());
            symbolTable.setType(varDeclaration.getIdentifier().getType());
            SymbolTable.top.put(symbolTable);
        } catch (ItemAlreadyExistsException e) {
            nameErrors.add(new VariableRedefinition(varDeclaration.getLine(), varDeclaration.getIdentifier().getName()));
        }
        return null;
    }
}

