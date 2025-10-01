// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.davidseptimus.maml.psi.MamlTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.davidseptimus.maml.psi.*;

public class MamlMembersImpl extends ASTWrapperPsiElement implements MamlMembers {

  public MamlMembersImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MamlVisitor visitor) {
    visitor.visitMembers(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MamlVisitor) accept((MamlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MamlKeyValue> getKeyValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MamlKeyValue.class);
  }

}
