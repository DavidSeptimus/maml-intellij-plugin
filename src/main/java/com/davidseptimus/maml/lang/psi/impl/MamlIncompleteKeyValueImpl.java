// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.lang.psi.impl;

import com.davidseptimus.maml.lang.psi.MamlIncompleteKeyValue;
import com.davidseptimus.maml.lang.psi.MamlInvalidKey;
import com.davidseptimus.maml.lang.psi.MamlKey;
import com.davidseptimus.maml.lang.psi.MamlVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MamlIncompleteKeyValueImpl extends ASTWrapperPsiElement implements MamlIncompleteKeyValue {

  public MamlIncompleteKeyValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MamlVisitor visitor) {
    visitor.visitIncompleteKeyValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MamlVisitor) accept((MamlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MamlInvalidKey getInvalidKey() {
    return findChildByClass(MamlInvalidKey.class);
  }

  @Override
  @Nullable
  public MamlKey getKey() {
    return findChildByClass(MamlKey.class);
  }

}
