package codigo;
import static codigo.Tokens.*;
%%
%class Lexer
%type Tokens
L=[a-zA-Z_]+
D=[0-9]+
espacio=[ ,\t,\r]+
%{
    public String lexeme;
%}
%%
int {lexeme=yytext(); return Int;}
float {lexeme=yytext(); return Float;}
string {lexeme=yytext(); return String;}
si | if {lexeme=yytext(); return If;}
else | sino {lexeme=yytext(); return Else;}
else if | o_sino {lexeme=yytext(); return Else_If;}
para | for {lexeme=yytext(); return For;}
mientras | while {lexeme=yytext(); return While;}
print | imprimir {lexeme=yytext(); return Imprimir;}
{espacio} {/*Ignore*/}
"//".* {/*Ignore*/}
"\n" {return Linea;}
"(" {lexeme=yytext(); return ParentesisAbierto;}
")" {lexeme=yytext(); return ParentesisCerrado;}
"{" {lexeme=yytext(); return LlaveAbierto;}
"}" {lexeme=yytext(); return LlaveCerrado;}
"[]" {lexeme=yytext(); return Arreglo;}
"==" {lexeme=yytext(); return OP_Igualdad;}
">" {lexeme=yytext(); return OP_Mayor;}
"<" {lexeme=yytext(); return OP_Menor;}
">=" {lexeme=yytext(); return OP_MayorOIgual;}
"<=" {lexeme=yytext(); return OP_MenorOIgual;}
"!=" {lexeme=yytext(); return OP_NoIgual;}
"=" {lexeme=yytext(); return Igual;}
"+" {lexeme=yytext(); return Suma;}
"-" {lexeme=yytext(); return Resta;}
"*" {lexeme=yytext(); return Multiplicacion;}
"**" {lexeme=yytext(); return Exponenciacion;}
"/" {lexeme=yytext(); return Division;}
"%" {lexeme=yytext(); return Modulo;}
"start" {lexeme=yytext(); return Start;}
{D}+"."{D}* {lexeme=yytext(); return NumeroDecimal;}
{L}({L}|{D})* {lexeme=yytext(); return Identificador;}
("(-"{D}+")")|{D}+ {lexeme=yytext(); return Numero;}
\"([^\"\n]|\\.)*\" {lexeme=yytext(); return Cadena;}
 . {return Error;}

