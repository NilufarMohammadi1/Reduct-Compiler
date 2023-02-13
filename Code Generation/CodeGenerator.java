package main.visitor.codeGenerator;

import main.ast.node.declaration.MainDeclaration;
import main.ast.node.Program;
import main.ast.node.declaration.ActorDeclaration;
import main.ast.node.declaration.ActorInstantiation;
import main.ast.node.declaration.VarDeclaration;
import main.ast.node.declaration.handler.*;
import main.ast.node.expression.*;
import main.ast.node.expression.values.*;
import main.ast.node.expression.operators.*;
import main.ast.node.statement.*;
import main.ast.type.*;
import main.ast.type.actorType.ActorType;
import main.ast.type.primitiveType.IntType;
import main.ast.type.primitiveType.StringType;
import main.visitor.Visitor;

import java.io.*;
import java.util.ArrayList;

public class CodeGenerator extends Visitor<String> {
    private String outputPath;
    private FileWriter currentFile;

    private int numOfUsedLabel;

    ArrayList<ActorDeclaration> programActors = new ArrayList<>();


    private ActorDeclaration currentActor;
    private HandlerDeclaration currentHandler;
    private MainDeclaration main;


    private void prepareOutputFolder() {
        this.outputPath = "output/";
        String jasminPath = "utilities/jasmin.jar";

        try{
            File directory = new File(this.outputPath);
            File[] files = directory.listFiles();
            if(files != null)
                for (File file : files)
                    file.delete();
            directory.mkdir();
        }
        catch(SecurityException e) {//never reached

        }
        copyFile(jasminPath, this.outputPath + "jasmin.jar");
    }

    private void copyFile(String toBeCopied, String toBePasted) {
        try {
            File readingFile = new File(toBeCopied);
            File writingFile = new File(toBePasted);
            InputStream readingFileStream = new FileInputStream(readingFile);
            OutputStream writingFileStream = new FileOutputStream(writingFile);
            byte[] buffer = new byte[1024];
            int readLength;
            while ((readLength = readingFileStream.read(buffer)) > 0)
                writingFileStream.write(buffer, 0, readLength);
            readingFileStream.close();
            writingFileStream.close();
        } catch (IOException e) {//never reached
        }
    }
    private void createFile(String name) {
        try {
            String path = this.outputPath + name + ".j";
            File file = new File(path);
            file.createNewFile();
            this.currentFile = new FileWriter(path);
        } catch (IOException e) {//never reached
        }
    }
    private void addCommand(String command) {
        try {
            command = String.join("\n\t\t", command.split("\n"));
            if(command.startsWith("Label_"))
                this.currentFile.write("\t" + command + "\n");
            else if(command.startsWith("."))
                this.currentFile.write(command + "\n");
            else
                this.currentFile.write("\t\t" + command + "\n");
            this.currentFile.flush();
        } catch (IOException e) {//never reached

        }
    }

    private String getFreshLabel(){
        String label = "Label_";
        label += numOfUsedLabel;
        numOfUsedLabel++;
        return label;
    }
    private int slotOf(String identifier) {
        int count = 1;
        for(VarDeclaration arg : currentHandler.getArgs()){
            if(arg.getIdentifier().getName().equals(identifier))
                return count;
            count++;
        }
        for(VarDeclaration var : currentHandler.getLocalVars())
        {
            if(var.getIdentifier().getName().equals(identifier))
                return count;
            count++;
        }
        return -1;
    }
    private int slotOfMain(String identifier) {
        int count = 1;
        for(VarDeclaration arg : main.getMainActors()){
            if(arg.getIdentifier().getName().equals(identifier))
                return count;
            count++;
        }
        return -1;
    }

    private Type getLocalVarType(Identifier id){
        for(VarDeclaration varDeclaration: currentHandler.getArgs()){
            if(id.getName().equals(varDeclaration.getIdentifier().getName()))
                return varDeclaration.getType();
        }
        for(VarDeclaration varDeclaration: currentHandler.getLocalVars()){
            if(id.getName().equals(varDeclaration.getIdentifier().getName()))
                return varDeclaration.getType();
        }
        return null;
    }
    private Type getActorVarType(Identifier id){
        for(VarDeclaration varDeclaration: currentActor.getActorVars()){
            if(id.getName().equals(varDeclaration.getIdentifier().getName()))
                return varDeclaration.getType();
        }
        return null;
    }
    private Type getKnownActorType(Identifier id){
        for(VarDeclaration varDeclaration: currentActor.getKnownActors()){
            if(id.getName().equals(varDeclaration.getIdentifier().getName()))
                return varDeclaration.getType();
        }
        return null;
    }

    private String getHandlerSignature(String actorName, String handlerName){
        String signature = "";

        for(ActorDeclaration actorDeclaration: programActors){
            if(actorDeclaration.getName().getName().equals(actorName)){
                if(handlerName.equals("initial")){
                    signature = "<init>(";
                    for(VarDeclaration varDeclaration:actorDeclaration.getKnownActors()){
                        signature += "L" +  ((ActorType)varDeclaration.getType()).getName() + ";";
                    }
                }
                for(HandlerDeclaration handlerDeclaration: actorDeclaration.getMsgHandlers()){
                    if(handlerDeclaration.getName().getName().equals(handlerName)){
                        signature = handlerName + "(";
                        for(VarDeclaration varDeclaration:handlerDeclaration.getArgs()){
                            if(varDeclaration.getType() instanceof IntType)
                                signature += "I";
                            else
                                signature += "Ljava/lang/String;";
                        }
                    }
                }
            }
        }
        signature += ")V";
        return signature;
    }

    public CodeGenerator() {
        this.prepareOutputFolder();
        this.numOfUsedLabel = 0;
    }


    @Override
    public String visit(Program program) {

        programActors.addAll(program.getActors());

        for (ActorDeclaration actorDeclaration : program.getActors())
            actorDeclaration.accept(this);


        program.getMain().accept(this);
        return null;
    }

    @Override
    public String visit(ActorDeclaration actorDeclaration) {
        currentActor = actorDeclaration;
        String actorName = actorDeclaration.getName().getName();
        createFile(actorName);

        //TODO add header
        addCommand(".class public " + actorName);
        addCommand(".super java/lang/Object");


        for(VarDeclaration varDeclaration: actorDeclaration.getKnownActors()){
            String fieldName = varDeclaration.getIdentifier().getName();
            Type fieldType = varDeclaration.getType();
            //TODO add known actor field
            addCommand(".field static public " + fieldName + " " + fieldType );
        }

        for(VarDeclaration varDeclaration: actorDeclaration.getActorVars()){
            String fieldName = varDeclaration.getIdentifier().getName();
            Type fieldType = varDeclaration.getType();
            //TODO add actor var field(it can be int or string)
            addCommand(".field static public " + fieldName + " " + fieldType );
        }

        if(actorDeclaration.getInitHandler() != null)
            actorDeclaration.getInitHandler().accept(this);

        for(MsgHandlerDeclaration msgHandlerDeclaration: actorDeclaration.getMsgHandlers())
            msgHandlerDeclaration.accept(this);


        return null;
    }

    @Override
    public String visit(InitHandlerDeclaration initHandlerDeclaration) {
        currentHandler = initHandlerDeclaration;

        String currentActorName = currentActor.getName().getName();

        //TODO add headers for constructor


        addCommand(".method public init()V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("aload 0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        addCommand("return");
        addCommand(".end method");

        addCommand(".method public static "+currentActorName+"(Ljava/lang/int;)V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");

        // TODO initialize known actors
        for(Statement statement: initHandlerDeclaration.getBody()){
            statement.accept(this);
        }

        //TODO add return and end method

        addCommand("return");
        addCommand(".end method");

        return null;
    }

    @Override
    public String visit(MsgHandlerDeclaration msgHandlerDeclaration) {
        currentHandler = msgHandlerDeclaration;
        String currentActorName = currentActor.getName().getName();
        String msgHandlerName = msgHandlerDeclaration.getName().getName();

        // TODO 1)add header 2)visit statements #)add "return" and "end method"
        addCommand(".method public static "+ getHandlerSignature(currentActorName, msgHandlerName));
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");

        for (Statement statement : msgHandlerDeclaration.getBody()){
            statement.accept(this);
        }

        addCommand("return");
        addCommand(".end method");
        return null;
    }

    @Override
    public String visit(ActorInstantiation actorInstantiation) {
        String actorName = ((ActorType)actorInstantiation.getType()).getName();

        addCommand("new " + actorName);
        addCommand("dup");

        for(Identifier knownActor : actorInstantiation.getKnownActors()){
            int slot = slotOfMain(knownActor.getName());
            addCommand("aload " + slot);
        }
        addCommand("invokespecial " + actorName + "/" + getHandlerSignature(actorName, "initial"));
        int slot = slotOfMain(actorInstantiation.getIdentifier().getName());
        addCommand("astore " + slot);

        return null;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        main = mainDeclaration;
        createFile("Main");
        // TODO add header class Main
        addCommand(".class public Main");
        addCommand(".super java/lang/Object");

        // TODO add default constructor

        addCommand(".method public init()V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("aload 0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        addCommand("return");
        addCommand(".end method");


        addCommand(".method public static main([Ljava/lang/String;)V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");


        for(ActorInstantiation actorInstantiation: mainDeclaration.getMainActors()){
            actorInstantiation.accept(this);
        }

        addCommand("return");
        addCommand(".end method");

        return null;
    }


    @Override
    public String visit(ConditionalStmt conditional) {
        //TODO based on condition goto right label (you should accept the and else bodies)

        addCommand(";hello ConditionalStmt " + conditional.getCondition());

        return null;
    }

    @Override
    public String visit(MsgHandlerCall msgHandlerCall) {
        String signature;
        Expression instance = msgHandlerCall.getInstance();
        String actorName;
        String msgHandlerName = msgHandlerCall.getMsgHandlerName().getName();
        if(instance instanceof Self){
            actorName = currentActor.getName().getName();
        }
        else{
            actorName = ((ActorType) getKnownActorType((Identifier) instance)).getName();
        }

        //TODO call appropriate handler

//        addCommand(";hello MsgHandlerCall " + actorName + "." + msgHandlerName );
        addCommand(".line " + msgHandlerCall.getLine() );
        addCommand("aload_0");
        addCommand("invokevirtual "+actorName+"/"+ getHandlerSignature(actorName, msgHandlerName));

        return null;
    }

    @Override
    public String visit(PrintStmt printStmt) {

        Expression arg = printStmt.getArg();
        Type argType;
        String typeOfPrint = "";
        if (arg instanceof Identifier){
            argType = getLocalVarType((Identifier) arg);

        }
        else if (arg instanceof ActorVarAccess){
            argType = getActorVarType(((ActorVarAccess) arg).getVariable());
        }
        else if (arg instanceof StringValue){
            argType = new StringType();
        }
        else {
            argType = new IntType();
        }


        //TODO print based on whether it is int or string

        if (argType!= null && argType.toString().equals(new StringType().toString())){
            typeOfPrint = "Ljava/lang/String;";
        }
        if (argType!= null && argType.toString().equals(new IntType().toString())){
            typeOfPrint = "I";
        }

        try {
            String argName = ((ActorVarAccess) arg).getVariable().getName() ;
            String handlerName = currentActor.getName().getName() ;

            addCommand(".line " + arg.getLine());
            addCommand("getstatic java/lang/System/out Ljava/io/PrintStream;");
            addCommand("aload_0");
            addCommand("getstatic "+ handlerName +"/"+  argName + " " + typeOfPrint);
            addCommand("invokevirtual java/io/PrintStream/println("+typeOfPrint+")V");
        }catch (Exception ex){

        }


//        addCommand(";hello print " + arg );
        return null;
    }

    @Override
    public String visit(AssignStmt assignStmt) {
        Expression lValue = assignStmt.getLValue();


        addCommand(";hello AssignStmt ");
        String rValueCommands = assignStmt.getRValue().accept(this);

        if(lValue instanceof Identifier identifier){
            Type type = getLocalVarType(identifier);
            int slot = slotOf(identifier.getName());
            //TODO store the variable based on whether it is int or string
            //addCommand(";hello lValue_Identifier " + slot + " "  + type.toString() );
            addCommand("istore " + slot);


        }
        else if(lValue instanceof ActorVarAccess actorVarAccess){
            String className = currentActor.getName().getName();
            String fieldName = actorVarAccess.getVariable().getName();
            Type varType = getActorVarType(actorVarAccess.getVariable());

            //TODO put the field based on whether it is int or string
            //addCommand(";hello lValue_ActorVarAccess " + className + " "  + fieldName + " " + varType);
            String typeString = "Ljava/lang/String";
            if (varType instanceof IntType){
                typeString = "I";
            }
            addCommand("putstatic "+className+"/"+fieldName + " " + typeString);
        }
        return null;
    }

    @Override
    public String visit(UnaryExpression unaryExpression) {
        UnaryOperator operator = unaryExpression.getUnaryOperator();
        String commands = "";
        commands += unaryExpression.getOperand().accept(this);
        if(operator == UnaryOperator.minus) {
            //TODO

            addCommand(";hello minus " + operator.toString() );

        }
        return commands;
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {
        BinaryOperator operator = binaryExpression.getBinaryOperator();
        String commands = "";
        commands += binaryExpression.getLeft().accept(this);
        commands += binaryExpression.getRight().accept(this);

        if (operator == BinaryOperator.add) {
            // TODO

            addCommand(";hello add " + commands);
        }
        else if (operator == BinaryOperator.sub) {
            //TODO
            addCommand(";hello sub " + commands);

        }
        else if (operator == BinaryOperator.mult) {
            //TODO
            addCommand(";hello mult " + commands);

        }
        else if(operator == BinaryOperator.eq) {
            //TODO
            addCommand(";hello eq " + commands);


        }
        return commands;
    }

    @Override
    public String visit(ActorVarAccess actorVarAccess) {
        String commands = "";
        String currentActorName = currentActor.getName().getName();
        String fieldName = actorVarAccess.getVariable().getName();
        Type varType = getActorVarType(actorVarAccess.getVariable());
        if(varType == null){
            return null;
        }
        //TODO get the field based on whether it is int or string
        if (varType.toString().equals(new StringType().toString())){

            commands = ";hello string " + fieldName;
        }
        if (varType.toString().equals(new IntType().toString())){

            commands = ";hello int " + fieldName;
        }
        addCommand(commands);
        return commands;
    }

    @Override
    public String visit(Identifier identifier) {
        String commands = "";
        String name = identifier.getName();
        Type type = getLocalVarType(identifier);

        //TODO load the local variable (iload for IntType, aload for String)

        addCommand(";hello identifier " + name + " -> " + type);


        if(type == null){
            type = getKnownActorType(identifier);
            String currentActorName = currentActor.getName().getName();
            String fieldName = identifier.getName();
            String actorTypeName = ((ActorType)type).getName();
            //TODO When the type is null it means the identifier is one of known actors, so load the field

            addCommand(";hello currentActorName " + fieldName + " -> " + actorTypeName);
        }
        return commands;
    }

    @Override
    public String visit(Self self) {
        String commands = "lconst_0";
        //TODO
        addCommand(commands);
        return commands;
    }

    @Override
    public String visit(IntValue value) {
        String commands = "ldc " + value.getConstant();
        //TODO

        addCommand(".line " + value.getLine());
        addCommand("aload_0");
        addCommand("pop");
        addCommand(commands);
        return commands;
    }

    @Override
    public String visit(StringValue value) {
        String commands = "ldc " + value.getConstant();
        //TODO

        addCommand(".line " + value.getLine());
        addCommand("aload_0");
        addCommand("pop");
        addCommand(commands);
        return commands;
    }

}

