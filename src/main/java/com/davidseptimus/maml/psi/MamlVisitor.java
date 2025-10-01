// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.davidseptimus.maml.MamlNamedElement;

public class MamlVisitor extends PsiElementVisitor {

  public void visitArray(@NotNull MamlArray o) {
    visitPsiElement(o);
  }

  public void visitItems(@NotNull MamlItems o) {
    visitPsiElement(o);
  }

  public void visitKey(@NotNull MamlKey o) {
    visitNamedElement(o);
  }

  public void visitKeyValue(@NotNull MamlKeyValue o) {
    visitPsiElement(o);
  }

  public void visitMembers(@NotNull MamlMembers o) {
    visitPsiElement(o);
  }

  public void visitObject(@NotNull MamlObject o) {
    visitPsiElement(o);
  }

  public void visitValue(@NotNull MamlValue o) {
    visitValueElement(o);
  }

  public void visitNamedElement(@NotNull MamlNamedElement o) {
    visitPsiElement(o);
  }

  public void visitValueElement(@NotNull MamlValueElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
