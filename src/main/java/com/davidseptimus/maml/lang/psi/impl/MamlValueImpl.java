// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.davidseptimus.maml.lang.psi.MamlTypes.*;
import com.davidseptimus.maml.lang.psi.MamlValueMixin;
import com.davidseptimus.maml.lang.psi.*;

public class MamlValueImpl extends MamlValueMixin implements MamlValue {

  public MamlValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MamlVisitor visitor) {
    visitor.visitValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MamlVisitor) accept((MamlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MamlArray getArray() {
    return findChildByClass(MamlArray.class);
  }

  @Override
  @Nullable
  public MamlInvalidValue getInvalidValue() {
    return findChildByClass(MamlInvalidValue.class);
  }

  @Override
  @Nullable
  public MamlObject getObject() {
    return findChildByClass(MamlObject.class);
  }

}
