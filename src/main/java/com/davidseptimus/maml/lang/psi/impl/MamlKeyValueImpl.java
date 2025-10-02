// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.lang.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.davidseptimus.maml.lang.psi.*;

public class MamlKeyValueImpl extends ASTWrapperPsiElement implements MamlKeyValue {

  public MamlKeyValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MamlVisitor visitor) {
    visitor.visitKeyValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MamlVisitor) accept((MamlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MamlKey getKey() {
    return findNotNullChildByClass(MamlKey.class);
  }

  @Override
  @Nullable
  public MamlValue getValue() {
    return findChildByClass(MamlValue.class);
  }

}
