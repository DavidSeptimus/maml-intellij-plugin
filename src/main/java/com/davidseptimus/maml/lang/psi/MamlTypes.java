// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.davidseptimus.maml.lang.psi.impl.*;

public interface MamlTypes {

  IElementType ARRAY = new MamlElementType("ARRAY");
  IElementType INCOMPLETE_KEY_VALUE = new MamlElementType("INCOMPLETE_KEY_VALUE");
  IElementType INVALID_VALUE = new MamlElementType("INVALID_VALUE");
  IElementType ITEMS = new MamlElementType("ITEMS");
  IElementType KEY = new MamlElementType("KEY");
  IElementType KEY_VALUE = new MamlElementType("KEY_VALUE");
  IElementType MEMBERS = new MamlElementType("MEMBERS");
  IElementType OBJECT = new MamlElementType("OBJECT");
  IElementType VALUE = new MamlElementType("VALUE");

  IElementType COLON = new MamlTokenType("COLON");
  IElementType COMMA = new MamlTokenType("COMMA");
  IElementType COMMENT = new MamlTokenType("COMMENT");
  IElementType FALSE = new MamlTokenType("FALSE");
  IElementType IDENTIFIER = new MamlTokenType("IDENTIFIER");
  IElementType LBRACE = new MamlTokenType("LBRACE");
  IElementType LBRACKET = new MamlTokenType("LBRACKET");
  IElementType MULTILINE_STRING = new MamlTokenType("MULTILINE_STRING");
  IElementType NULL = new MamlTokenType("NULL");
  IElementType NUMBER = new MamlTokenType("NUMBER");
  IElementType RBRACE = new MamlTokenType("RBRACE");
  IElementType RBRACKET = new MamlTokenType("RBRACKET");
  IElementType STRING = new MamlTokenType("STRING");
  IElementType TRUE = new MamlTokenType("TRUE");
  IElementType UNTERMINATED_STRING = new MamlTokenType("UNTERMINATED_STRING");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ARRAY) {
        return new MamlArrayImpl(node);
      }
      else if (type == INCOMPLETE_KEY_VALUE) {
        return new MamlIncompleteKeyValueImpl(node);
      }
      else if (type == INVALID_VALUE) {
        return new MamlInvalidValueImpl(node);
      }
      else if (type == ITEMS) {
        return new MamlItemsImpl(node);
      }
      else if (type == KEY) {
        return new MamlKeyImpl(node);
      }
      else if (type == KEY_VALUE) {
        return new MamlKeyValueImpl(node);
      }
      else if (type == MEMBERS) {
        return new MamlMembersImpl(node);
      }
      else if (type == OBJECT) {
        return new MamlObjectImpl(node);
      }
      else if (type == VALUE) {
        return new MamlValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
