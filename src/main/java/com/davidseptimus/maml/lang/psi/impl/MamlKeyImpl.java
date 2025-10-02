// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.lang.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.davidseptimus.maml.lang.psi.MamlKeyMixin;
import com.davidseptimus.maml.lang.psi.*;

public class MamlKeyImpl extends MamlKeyMixin implements MamlKey {

  public MamlKeyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MamlVisitor visitor) {
    visitor.visitKey(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MamlVisitor) accept((MamlVisitor)visitor);
    else super.accept(visitor);
  }

}
