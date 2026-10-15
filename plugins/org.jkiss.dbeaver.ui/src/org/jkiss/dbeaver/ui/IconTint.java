/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2026 DBeaver Corp and others
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

package org.jkiss.dbeaver.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;

/**
 * Softens icon colors for dark themes: lowers saturation and blends a little of the
 * theme foreground color in, so that bright icons do not clash with a muted palette.
 */
final class IconTint {
    private static final float SATURATION_FACTOR = 0.8f;
    private static final float BLEND_RATIO = 0.08f;
    private static final RGB BLEND_COLOR = new RGB(0xcd, 0xd6, 0xf4);

    private IconTint() {
    }

    /**
     * Wraps descriptor so that image data is tinted for every requested zoom (keeps HiDPI icons sharp).
     */
    @NotNull
    static ImageDescriptor tint(@NotNull ImageDescriptor source) {
        return ImageDescriptor.createFromImageDataProvider(zoom -> {
            ImageData data = source.getImageData(zoom);
            return data == null ? null : tint(data);
        });
    }

    @Nullable
    private static ImageData tint(@NotNull ImageData src) {
        PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
        ImageData out = new ImageData(src.width, src.height, 32, palette);
        boolean pixelTransparency = src.getTransparencyType() == SWT.TRANSPARENCY_PIXEL;
        for (int y = 0; y < src.height; y++) {
            for (int x = 0; x < src.width; x++) {
                int pixel = src.getPixel(x, y);
                int alpha = pixelTransparency && pixel == src.transparentPixel ? 0 : src.getAlpha(x, y);
                out.setAlpha(x, y, alpha);
                if (alpha == 0) {
                    continue;
                }
                RGB rgb = src.palette.getRGB(pixel);
                float[] hsb = rgb.getHSB();
                RGB soft = new RGB(hsb[0], hsb[1] * SATURATION_FACTOR, hsb[2]);
                int r = blend(soft.red, BLEND_COLOR.red);
                int g = blend(soft.green, BLEND_COLOR.green);
                int b = blend(soft.blue, BLEND_COLOR.blue);
                out.setPixel(x, y, palette.getPixel(new RGB(r, g, b)));
            }
        }
        return out;
    }

    private static int blend(int value, int with) {
        return Math.round(value * (1 - BLEND_RATIO) + with * BLEND_RATIO);
    }
}
