package com.davidseptimus.maml;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.davidseptimus.maml.psi.MamlTypes.*;

%%

%{
  public MamlLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class MamlLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

// Whitespace and newlines
WS=[ \t]
NL=\r\n|\n|\r

// String characters - excluding backslash and quote
STRING_CHAR=[^\\\"\u0000-\u001F]
ESCAPE_SEQ=\\[\\\"\/nrt]|\\u\{[0-9A-Fa-f]{1,6}\}

// Multiline string - any character except quote, or quoted sequences
ML_CHAR=[^\"]|\"[^\"]|\"\"[^\"]

// Numbers
DIGIT=[0-9]
ONENINE=[1-9]
INTEGER=0|{ONENINE}{DIGIT}*
FRACTION=\.{DIGIT}+
EXPONENT=[eE][+-]?{DIGIT}+
NUMBER=-?{INTEGER}{FRACTION}?{EXPONENT}?

// Identifier - alphanumeric, dash, underscore
IDENTIFIER=[a-zA-Z0-9_-]+

// Comment - hash to end of line
COMMENT=#[^\r\n]*

%%
<YYINITIAL> {
  // Whitespace (spaces, tabs, newlines)
  {WS}+                    { return WHITE_SPACE; }
  {NL}+                    { return WHITE_SPACE; }

  // Comments
  {COMMENT}                { return COMMENT; }

  // Structural tokens
  "{"                      { return LBRACE; }
  "}"                      { return RBRACE; }
  "["                      { return LBRACKET; }
  "]"                      { return RBRACKET; }
  ":"                      { return COLON; }
  ","                      { return COMMA; }

  // Literal values
  "true"                   { return TRUE; }
  "false"                  { return FALSE; }
  "null"                   { return NULL; }

  // Multiline string (must come before regular string)
  \"\"\"({ML_CHAR})*\"\"\" { return MULTILINE_STRING; }

  // Regular string
  \"({STRING_CHAR}|{ESCAPE_SEQ})*\" { return STRING; }

  // Number
  {NUMBER}                 { return NUMBER; }

  // Identifier (unquoted key)
  {IDENTIFIER}             { return IDENTIFIER; }

}

[^] { return BAD_CHARACTER; }
