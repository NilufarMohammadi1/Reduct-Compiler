package main.visitor.typeAnalyzer;

import main.ast.node.Program;
import main.ast.node.declaration.*;
import main.ast.node.declaration.handler.*;
import main.ast.node.expression.*;
import main.ast.node.expression.operators.*;
import main.ast.node.statement.*;
import main.ast.type.*;
import main.ast.type.actorType.*;
//import main.ast.type.primitiveType.*;
import main.ast.type.primitiveType.BoolType;
import main.ast.type.primitiveType.IntType;
import main.ast.type.primitiveType.StringType;
import main.compileError.*;
import main.compileError.type.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.itemException.*;
import main.symbolTable.symbolTableItems.*;
import main.visitor.Visitor;
//import main.ast.type.primitiveType.IntType;
//import main.ast.type.primitiveType.BoolType;
//import main.ast.type.primitiveType.StringType;

import java.util.ArrayList;
import java.util.Objects;

public class TypeAnalyzer extends Visitor<Void> {
    public ArrayList<CompileError> typeErrors = new ArrayList<>();
    ExpressionTypeChecker expressionTypeChecker = new ExpressionTypeChecker(typeErrors);

    @Override
    public Void visit(Program program) {
        for (ActorDeclaration actorDeclaration : program.getActors()) {
            actorDeclaration.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ActorDeclaration actorDeclaration){
        try {
            ActorItem actorItem = (ActorItem) SymbolTable.root.get(ActorItem.STARTKEY + actorDeclaration.getName().getName());
            SymbolTable.push(actorItem.getActorSymbolTable());
        }catch (ItemNotFoundException e){
            System.out.println(actorDeclaration.getName() + " Error -> " + actorDeclaration.getLine());
            // unreachable
        }

        for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
            knownActor.accept(this);
        }

        for (VarDeclaration actorVar : actorDeclaration.getActorVars()) {
            actorVar.accept(this);
        }

        for (HandlerDeclaration actorHandler: actorDeclaration.getMsgHandlers()) {
            actorHandler.accept(this);
        }
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(HandlerDeclaration handlerDeclaration){
        try {
            HandlerItem handlerTableItem = (HandlerItem) SymbolTable.top.get(HandlerItem.STARTKEY + handlerDeclaration.getName().getName());
            SymbolTable.push(handlerTableItem.getHandlerSymbolTable());
        }catch (ItemNotFoundException e){
            System.out.println(handlerDeclaration.getName() + " Error -> " + handlerDeclaration.getLine());
            // unreachable
        }

        for (VarDeclaration varDeclaration: handlerDeclaration.getArgs())
            varDeclaration.accept(this);

        for (VarDeclaration varDeclaration: handlerDeclaration.getLocalVars())
            varDeclaration.accept(this);

        for (Statement statement: handlerDeclaration.getBody())
            statement.accept(this);

        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditional) {
        Type conditionType = conditional.getCondition().accept(expressionTypeChecker);
        if (conditionType != null){
            typeErrors.add(new ConditionTypeNotBool(conditional.getLine()));
        }

        //TODO check errors
        for (Statement stmt: conditional.getThenBody())
            stmt.accept(this);

        if(conditional.getElseBody() != null){
            for (Statement stmt: conditional.getElseBody())
                stmt.accept(this);
        }
        return null;
    }


    @Override
    public Void visit(MsgHandlerCall msgHandlerCall) {
        Type instanceType = msgHandlerCall.getInstance().accept(expressionTypeChecker);
        //TODO check errors
        String msgHandlerName = msgHandlerCall.getMsgHandlerName().getName();
        ActorItem symbolTable = null;
        if ( instanceType.toString().equals(new StringType().toString()) ||
                instanceType.toString().equals(new IntType().toString()) ||
                instanceType.toString().equals(new BoolType().toString())){
            typeErrors.add(new CallOnNonActor(msgHandlerCall.getLine()));
            return null;
        }
        try {
            symbolTable = ((ActorItem)SymbolTable.root.get(ActorItem.STARTKEY + instanceType.toString()));
            symbolTable.getActorSymbolTable().get(HandlerItem.STARTKEY + msgHandlerName);
        } catch (ItemNotFoundException e) {
            if(symbolTable != null){
                typeErrors.add(new HandlerNotDeclared(msgHandlerCall.getLine(), msgHandlerName,  instanceType.toString()));
            }
        }

        for (Expression arg: msgHandlerCall.getArgs())
            arg.accept(expressionTypeChecker);

        return null;
    }

    @Override
    public Void visit(AssignStmt assignStmt) {
        Type tl = assignStmt.getLValue().accept(expressionTypeChecker);
        Type tr = assignStmt.getRValue().accept(expressionTypeChecker);

        //TODO check errors
        if (tl == null){
            typeErrors.add(new LeftSideNotLValue(assignStmt.getLine()));
            return null;
        }
        if(tr == null){
            try{
                var x = ((BinaryExpression) assignStmt.getRValue());
            }catch (Exception ex){
                typeErrors.add(new UnsupportedOperandType(assignStmt.getLine(), "assign"));
            }
            return null;
        }

        if (!tl.toString().equals(tr.toString()) && !tr.toString().equals(new NoType().toString())){

            typeErrors.add(new UnsupportedOperandType(assignStmt.getLine(), tl.toString()));
            return null;
        }


        return null;
    }

    @Override
    public Void visit(VarDeclaration varDeclaration) {
        //TODO check errors
        SymbolTableItem item;
        String name = varDeclaration.getIdentifier().getName();
        try {
            HandlerItem handlerItem = (HandlerItem)SymbolTable.top.get(VariableItem.STARTKEY + name);
//            Type t =  handlerItem.getHandlerDeclaration().getName().getType();
//            System.out.println("HandlerItem-> " + handlerItem.getKey());
        } catch (Exception e2) {
            try {
                VariableItem variableItem = (VariableItem)SymbolTable.top.get(VariableItem.STARTKEY + name);
                Type t =  variableItem.getVarDeclaration().getType();

                if (!new IntType().toString().equals(t.toString()) &&
                        !new StringType().toString().equals(t.toString()) &&
                        !new BoolType().toString().equals(t.toString())){
                    // known actors
                    String actorName = variableItem.getVarDeclaration().getType().toString();
                    try {
                        SymbolTable.root.get(ActorItem.STARTKEY + actorName);
                    }catch (ItemNotFoundException exception){
                        typeErrors.add(new ActorNotDeclared(varDeclaration.getLine(), actorName));
                    }
                }else{
                    // variable
//                    System.out.println("VariableItem -> " + variableItem.getName() + "  " + t);
                }
            } catch (ItemNotFoundException e3) {
                typeErrors.add(new VarNotDeclared(varDeclaration.getLine(), name));
            }
        }


        return null;
    }

}

