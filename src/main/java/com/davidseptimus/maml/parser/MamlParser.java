// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.davidseptimus.maml.psi.MamlTypes.*;
import static com.davidseptimus.maml.parser.MamlParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class MamlParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return mamlFile(b, l + 1);
  }

  /* ********************************************************** */
  // LBRACKET items? RBRACKET
  public static boolean array(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && array_1(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, ARRAY, r);
    return r;
  }

  // items?
  private static boolean array_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_1")) return false;
    items(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // value (COMMA? value)* COMMA?
  public static boolean items(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "items")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ITEMS, "<items>");
    r = value(b, l + 1);
    r = r && items_1(b, l + 1);
    r = r && items_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA? value)*
  private static boolean items_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "items_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!items_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "items_1", c)) break;
    }
    return true;
  }

  // COMMA? value
  private static boolean items_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "items_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = items_1_0_0(b, l + 1);
    r = r && value(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean items_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "items_1_0_0")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // COMMA?
  private static boolean items_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "items_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // STRING | IDENTIFIER
  public static boolean key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key")) return false;
    if (!nextTokenIs(b, "<key>", IDENTIFIER, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEY, "<key>");
    r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // key COLON value
  public static boolean key_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key_value")) return false;
    if (!nextTokenIs(b, "<key value>", IDENTIFIER, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEY_VALUE, "<key value>");
    r = key(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && value(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // value?
  static boolean mamlFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mamlFile")) return false;
    value(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // key_value (COMMA? key_value)* COMMA?
  public static boolean members(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "members")) return false;
    if (!nextTokenIs(b, "<members>", IDENTIFIER, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MEMBERS, "<members>");
    r = key_value(b, l + 1);
    r = r && members_1(b, l + 1);
    r = r && members_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA? key_value)*
  private static boolean members_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "members_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!members_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "members_1", c)) break;
    }
    return true;
  }

  // COMMA? key_value
  private static boolean members_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "members_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = members_1_0_0(b, l + 1);
    r = r && key_value(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean members_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "members_1_0_0")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // COMMA?
  private static boolean members_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "members_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // LBRACE members? RBRACE
  public static boolean object(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && object_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, OBJECT, r);
    return r;
  }

  // members?
  private static boolean object_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_1")) return false;
    members(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // object | array | STRING | MULTILINE_STRING | NUMBER | TRUE | FALSE | NULL | COMMENT
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VALUE, "<value>");
    r = object(b, l + 1);
    if (!r) r = array(b, l + 1);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, MULTILINE_STRING);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, NULL);
    if (!r) r = consumeToken(b, COMMENT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
