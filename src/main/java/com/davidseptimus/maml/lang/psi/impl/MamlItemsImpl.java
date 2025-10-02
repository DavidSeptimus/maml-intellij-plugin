// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.davidseptimus.maml.lang.psi.*;

public class MamlItemsImpl extends ASTWrapperPsiElement implements MamlItems {

  public MamlItemsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MamlVisitor visitor) {
    visitor.visitItems(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MamlVisitor) accept((MamlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MamlValue> getValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MamlValue.class);
  }

}
