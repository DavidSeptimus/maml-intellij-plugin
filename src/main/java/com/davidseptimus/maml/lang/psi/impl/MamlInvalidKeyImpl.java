// This is a generated file. Not intended for manual editing.
package com.davidseptimus.maml.lang.psi.impl;

import com.davidseptimus.maml.lang.psi.MamlInvalidKey;
import com.davidseptimus.maml.lang.psi.MamlVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class MamlInvalidKeyImpl extends ASTWrapperPsiElement implements MamlInvalidKey {

    public MamlInvalidKeyImpl(@NotNull ASTNode node) {
        super(node);
    }

    public void accept(@NotNull MamlVisitor visitor) {
        visitor.visitInvalidKey(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof MamlVisitor) accept((MamlVisitor) visitor);
        else super.accept(visitor);
    }

}
