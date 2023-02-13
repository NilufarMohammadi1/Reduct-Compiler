grammar reduct;

@header{
    import main.ast.node.*;
    import main.ast.node.declaration.*;
    import main.ast.node.declaration.handler.*;
    import main.ast.node.statement.*;
    import main.ast.node.expression.*;
    import main.ast.node.expression.operators.*;
    import main.ast.node.expression.values.*;
    import main.ast.type.primitiveType.*;
    import main.ast.type.actorType.*;
    import main.ast.type.*;
}
@members{
    void print(String str, Boolean... params){
        assert params.length <= 1;
        boolean inline = params.length > 0 ? params[0].booleanValue() : false;
        if (inline) {
            System.out.print(str);
        } else
            System.out.println(str);
    }
}
// do not change first rule (program) name
program returns [Program p]:
    {$p = new Program(); $p.setLine(0);}
    (actordec = actorDeclaration {$p.addActor($actordec.actordec);})+
    programMain = mainDeclaration {$p.setMain($programMain.mainDecRet);}
;

actorDeclaration returns [ActorDeclaration actordec]:
    ACTOR IDENTIFIER (|EXTENDS IDENTIFIER)
                LPAR INT_VALUE RPAR NL*
                BEGIN NL* actorBody NL* END

;

mainDeclaration returns [MainDeclaration mainDecRet]:
    MAIN  BEGIN NL+
                (IDENTIFIER IDENTIFIER dynamicParams
                 (SEMICOLLON | (COLON dynamicParams)) interaSpace)* END (NL+ | )
;


actorBody:
            (declareKnownActors| ) NL+
            (declareActorVars| )
            ((msghandler)*| ) |
            (msgHandlerBody| );



returnStatement:
            RETURN (expression)?;


functionArguments:
                (variableWithType)
                (() | (COMMA variableWithType)*) |
                ();

variableWithType:
            type id=IDENTIFIER ;

type:
            (//declareActor |
            primitiveDataType |
            declareKnownActors |
            declareActorVars |
//            mainBody |
            msghandler)*;


//mainBody: MAIN  BEGIN
//            (IDENTIFIER IDENTIFIER dynamicParams
//             (SEMICOLLON | (COLON dynamicParams)) interaSpace)* END (NL+ | );

declareKnownActors:
                    KNOWNACTORS
                    BEGIN NL*
                    (declareInterface interaSpace)*
                    END NL+;

declareInterface: (IDENTIFIER
                    IDENTIFIER SEMICOLLON |
                    primitiveDataType IDENTIFIER SEMICOLLON);
multiStatements:
            begin
            (statement interaSpace)*
            statement (SEMICOLLON)?
            end;

singleStatement:
            NL+ statement;
functionScope:
            multiStatements |
            singleStatement (SEMICOLLON)?;
ifStatement: IF (LPAR expression RPAR )
            BEGIN ((functionScope interaSpace)* | ) END   NL*
            (() |  NL* ELSE BEGIN ( | functionScope) NL* END)? ;
declareActorVars: ACTORVARS BEGIN NL*
                    (type IDENTIFIER interaSpace)*|
                    END
                    NL*;
declareLocalVariablesMsgHandler:
                ((primitiveDataType| )
                IDENTIFIER SEMICOLLON
                interaSpace)*;

functionCallStatement:
            accessExpression ((DOT IDENTIFIER (LPAR functionCallArguments RPAR)+) |
            (DOT IDENTIFIER) |
            (LBRACK expression RBRACK))*
            ((DOT IDENTIFIER)? (LPAR functionCallArguments RPAR)+);
assignmentStatement:
            orExpression ASSIGN expression;
statement: assignmentStatement |
            ifStatement|
            defaultFunctionStatement |
            functionCallStatement |
            returnStatement |
            displayStatement;

msghandler: MSGHANDLER
            (IDENTIFIER|INITIAL)
            (LPAR (functionArguments| ) RPAR)
            BEGIN NL+ ((declareLocalVariablesMsgHandler| )
            (statement SEMICOLLON NL+)*)
            ((selfBody|selfBody2|)) END NL+;

msgHandlerBody: (IDENTIFIER ASSIGN
                (STRING_VALUE|INT_VALUE) SEMICOLLON
                interaSpace |
                IDENTIFIER DOT IDENTIFIER
                LPAR (IDENTIFIER | ) RPAR
                SEMICOLLON interaSpace)*;

primitiveDataType:
            STRING |
            INT |
            BOOL;

selfBody: (
            (SELF DOT IDENTIFIER)
            ASSIGN (values|IDENTIFIER)
            SEMICOLLON interaSpace)*;

dynamicParams: LPAR
                ((expression
                (COMMA| ))* | ())
                RPAR;
selfBody2: (SELF DOT IDENTIFIER
            dynamicParams SEMICOLLON)*;
values:
            boolValue |
            INT_VALUE |
            STRING_VALUE;

boolValue:
            TRUE |
            FALSE;


interaSpace:
        (SEMICOLLON NL+) |
        NL+ |
        SEMICOLLON;

begin:
    BEGIN NL+;

end:
    NL+ END;

expression:
            orExpression (ASSIGN expression)?;

displayStatement:
            DISPLAY
            LPAR (STRING_PRINT | expression) RPAR;

defaultFunctionStatement:
            displayStatement ;


orExpression:
            andExpression
            (op = OR  andExpression )*;

andExpression:
            equalityExpression
            (op = AND equalityExpression )*;

equalityExpression:
            relationalExpression
            (op = EQUAL relationalExpression )*;

relationalExpression:
            additiveExpression
            (op = (GREATER_THAN | LESS_THAN) additiveExpression )*;

additiveExpression:
            multiplicativeExpression
            (op = (PLUS | MINUS) multiplicativeExpression)*;

multiplicativeExpression:
            unaryExpression (op = (MULT | DIVIDE)
            unaryExpression )*;

unaryExpression:
            op = (NOT | MINUS)
            unaryExpression  |
            accessExpression;

accessExpression:
            otherExpression
            (((DOT IDENTIFIER)? LPAR functionCallArguments RPAR) |
            (DOT IDENTIFIER) |
            (LBRACK expression RBRACK))*;

otherExpression:
            values |
            defaultFunctionStatement |
            IDENTIFIER |
            LPAR (expression) RPAR |
            IDENTIFIER LBRACK expression RBRACK;

functionCallArguments:
            expression (() |
            (COMMA expression)*) |
            ();


BEGIN: '{';
END: '}';

INT: 'int';
STRING: 'string';
BOOL: 'boolean';
VOID: 'void';
IF: 'if';
ELSE: 'else';
RETURN: 'return';
MAIN: 'main';

DISPLAY: 'print';
APPEND: 'append';
SIZE: 'size';

TRUE: 'true';
FALSE: 'false';

ARROW: '->';
GREATER_THAN: '>';
LESS_THAN: '<';
EQUAL: '==';

MULT: '*';
DIVIDE: '/';
PLUS: '+';
MINUS: '-';
AND: '&';
OR: '|';
NOT: '~';

ASSIGN: '=';

COT: '"';
LPAR: '(';
RPAR: ')';
LBRACK: '[';
RBRACK: ']';

DOT: '.';
HASH: '#';
COMMA: ',';
SEMICOLLON: ';';
COLON: ':';
ACTOR: 'actor';
KNOWNACTORS: 'knownactors';
ACTORVARS: 'actorvars';
EXTENDS: 'extends';
MSGHANDLER: 'msghandler';
INITIAL: 'initial';
SELF: 'self';
STRING_VALUE: '""' | '"' IDENTIFIER '"';
INT_VALUE: '0' | [1-9][0-9]*;
IDENTIFIER: [a-zA-Z_][A-Za-z0-9_]*;
NL: '\r'? '\n';
COMMENT: '//' (.)*? NL -> skip;
WS: [ \t|] -> skip;
STRING_PRINT: '"' ( '\\' ~[\r\n] | ~[\\"\r\n] )* '"';
