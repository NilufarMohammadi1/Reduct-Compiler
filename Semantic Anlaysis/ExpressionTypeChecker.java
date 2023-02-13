package main.visitor.typeAnalyzer;

import main.ast.node.expression.*;
import main.ast.node.expression.values.*;
import main.ast.type.*;
import main.ast.type.actorType.*;
import main.compileError.*;
import main.ast.node.expression.operators.*;
import main.compileError.type.*;
import main.visitor.*;
import main.ast.type.primitiveType.*;
import main.symbolTable.*;
import main.symbolTable.symbolTableItems.*;
import main.symbolTable.itemException.*;
import java.util.ArrayList;

public class ExpressionTypeChecker extends Visitor<Type> {
    public ArrayList<CompileError> typeErrors;
    public ExpressionTypeChecker(ArrayList<CompileError> typeErrors){
        this.typeErrors = typeErrors;
    }

    public boolean sameType(Type el1,Type el2){
        //TODO check the two type are same or not
        return el1.toString().equals(el2.toString());

    }

    public boolean isLvalue(Expression expr){
        //TODO check the expr are lvalue or not

        return false;
    }


    @Override
    public Type visit(UnaryExpression unaryExpression) {

        Expression uExpr = unaryExpression.getOperand();
        Type expType = uExpr.accept(this);
        UnaryOperator operator = unaryExpression.getUnaryOperator();

        //TODO check errors and return the type

        return null;
    }

    @Override
    public Type visit(BinaryExpression binaryExpression) {
        Type tl = binaryExpression.getLeft().accept(this);
        Type tr = binaryExpression.getRight().accept(this);
//        if(binaryExpression.getLine() == 25){
//            System.out.println("Hello");
//        }
        BinaryOperator operator =  binaryExpression.getBinaryOperator();
        if(operator.equals(BinaryOperator.eq)) {
            //TODO check errors and return the type
            if(!sameType(tl, tr)){
                typeErrors.add(new LeftSideNotLValue(binaryExpression.getLine()));
            }
        }
        else { // + -  *
            //TODO check errors and return the type
            if(tl == null || tr == null ){
                BinaryOperator op;
                if(tr == null){
                    op = ((BinaryExpression) binaryExpression.getRight()).getBinaryOperator();
                }else{
                    op = ((BinaryExpression) binaryExpression.getLeft()).getBinaryOperator();
                }
                typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), op.name()));
                return new NoType() ;
            }else{

                if (!tl.toString().equals(new NoType().toString())){
                    return null;
                }
                if (tr.toString().equals(new NoType().toString())){
                    return null;
                }

//                System.out.println("Hello");
//                if (!sameType(tl, tr)){
////                    var op = ((BinaryExpression) binaryExpression.getBinaryOperator()).getBinaryOperator();
//                    typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().name()));
//                    return null;
//                }
            }
            //                VariableItem variableItemLeft = (VariableItem)SymbolTable.top.get(VariableItem.STARTKEY + leftName);
//                VariableItem variableItemRight = (VariableItem)SymbolTable.top.get(VariableItem.STARTKEY + rightName);
//            try {
//                Type leftType = binaryExpression.getLeft().accept(this);
//                Type rightType = binaryExpression.getRight().accept(this);
//                if (leftType.toString().equals(new NoType().toString())){
//                    return new NoType();
//                }
//                if (rightType.toString().equals(new NoType().toString())){
//                    return new NoType();
//                }
//                if (!leftType.toString().equals(rightType.toString())){
//                    typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(),
//                            binaryExpression.getBinaryOperator().name()));
//                }else{
//                    return leftType;
//                }

//            } catch (Exception e) {
////                throw new RuntimeException(e);
//            }
//            System.out.println(binaryExpression.getLine() + "->" + binaryExpression.getLeft().getType() + "  " +
//                                            binaryExpression.getBinaryOperator() + "  " +
//                    binaryExpression.getRight().getType());
//            else{
////                if(!sameType(tl, tr)){
////                    typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().name()));
////                }
//            }
        }
//        tl.toString().equals(new NoType().toString()) ||
//                tr.toString().equals(new NoType().toString())/
        return null;
    }

    @Override
    public Type visit(ActorVarAccess actorVarAccess) {
        //TODO check errors and return the type
        try {
            VariableItem item = (VariableItem)SymbolTable.top.get(VariableItem.STARTKEY + actorVarAccess.getVariable().getName());
            return item.getVarDeclaration().getType();
        } catch (ItemNotFoundException e) {
            typeErrors.add(new VarNotDeclared(actorVarAccess.getLine(), actorVarAccess.getVariable().getName()));
        }
        return new NoType();

    }

    @Override
    public Type visit(Identifier identifier) {
        //TODO check errors and return the type
        try {
            VariableItem variableItem = (VariableItem) SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName());
            return variableItem.getVarDeclaration().getType();
        } catch (ItemNotFoundException e) {
            typeErrors.add(new VarNotDeclared(identifier.getLine(), identifier.getName()));
            return new NoType();
        }
    }


    @Override
    public Type visit(Self self) {
        String currentActorName = SymbolTable.top.pre.name;
        return new ActorType(currentActorName);
    }

    @Override
    public Type visit(IntValue value) {
        return new IntType();
    }

    @Override
    public Type visit(StringValue value) {
        return new StringType();
    }
}
