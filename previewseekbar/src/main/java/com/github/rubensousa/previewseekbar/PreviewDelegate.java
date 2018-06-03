/*
 * Copyright 2018 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rubensousa.previewseekbar;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.rubensousa.previewseekbar.base.PreviewLoader;
import com.github.rubensousa.previewseekbar.base.PreviewView;

public class PreviewDelegate implements PreviewView.OnPreviewChangeListener {

    private FrameLayout previewFrameLayout;
    private View morphView;
    private View previewFrameView;
    private PreviewAnimator animator;
    private PreviewView previewView;
    private PreviewLoader previewLoader;
    private ViewGroup previewParent;

    private int scrubberColor;
    private boolean showing;
    private boolean startTouch;
    private boolean setup;
    private boolean enabled;

    public PreviewDelegate(PreviewView previewView, ViewGroup parent, int scrubberColor) {
        this(previewView, parent, scrubberColor, null);
    }

    public PreviewDelegate(PreviewView previewView, ViewGroup parent, int scrubberColor,
                           @Nullable FrameLayout previewFrameLayout) {
        this.previewView = previewView;
        this.previewView.addOnPreviewChangeListener(this);
        this.previewParent = parent;
        this.scrubberColor = scrubberColor;
        if (previewFrameLayout != null) {
            attachPreviewFrameLayout(previewFrameLayout);
        }
    }

    public void setPreviewLoader(PreviewLoader previewLoader) {
        this.previewLoader = previewLoader;
    }

    public void attachPreviewFrameLayout(FrameLayout frameLayout) {
        if (setup) {
            return;
        }
        this.previewFrameLayout = frameLayout;
        inflateViews(frameLayout);
        morphView.setVisibility(View.INVISIBLE);
        previewFrameLayout.setVisibility(View.INVISIBLE);
        previewFrameView.setVisibility(View.INVISIBLE);
        animator = new PreviewAnimatorLollipopImpl(previewParent, previewView, morphView,
                previewFrameLayout, previewFrameView);
        setup = true;
    }

    public boolean isShowing() {
        return showing;
    }

    public void show() {
        if (!showing && setup) {
            animator.show();
            showing = true;
        }
    }

    public void hide() {
        if (showing) {
            animator.hide();
            showing = false;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPreviewColorTint(@ColorInt int color) {
        Drawable drawable = DrawableCompat.wrap(morphView.getBackground());
        DrawableCompat.setTint(drawable, color);
        morphView.setBackground(drawable);
        previewFrameView.setBackgroundColor(color);
    }

    public void setPreviewColorResourceTint(@ColorRes int color) {
        setPreviewColorTint(ContextCompat.getColor(previewParent.getContext(), color));
    }

    @Override
    public void onStartPreview(PreviewView previewView) {
        startTouch = true;
    }

    @Override
    public void onStopPreview(PreviewView previewView) {
        if (showing) {
            animator.hide();
        }
        showing = false;
        startTouch = false;
    }

    @Override
    public void onPreview(PreviewView previewView, int progress, boolean fromUser) {
        if (setup && enabled) {
            animator.move();
            if (!showing && !startTouch && fromUser) {
                show();
            }
            if (previewLoader != null) {
                previewLoader.loadPreview(progress, previewView.getMax());
            }
        }
        startTouch = false;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void inflateViews(FrameLayout frameLayout) {

        // Create morph view
        View morphView = new View(frameLayout.getContext());
        morphView.setBackgroundResource(R.drawable.previewseekbar_morph);

        // Setup morph view
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(0, 0);
        layoutParams.width = frameLayout.getResources()
                .getDimensionPixelSize(R.dimen.previewseekbar_indicator_width);
        layoutParams.height = layoutParams.width;
        frameLayout.addView(morphView, layoutParams);

        // Create frame view for the circular reveal
        View frameView = new View(frameLayout.getContext());
        FrameLayout.LayoutParams frameLayoutParams
                = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(frameView, frameLayoutParams);

        // Apply same color for the morph and frame views
        setPreviewColorTint(scrubberColor);
    }
}