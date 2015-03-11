/*
 * Copyright 2015 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.impl.jpa.function;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class TemplateRenderer {

    private final TemplateEmitter[] emitters;

    public TemplateRenderer(String template) {
        List<TemplateEmitter> emitterList = new ArrayList<TemplateEmitter>();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            
            if (c == '?') {
                emitterList.add(new StringEmitter(sb.toString()));
                sb.setLength(0);
                
                while (++i < template.length()) {
                    c = template.charAt(i);
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    } else {
                        emitterList.add(new ParameterEmitter(Integer.valueOf(sb.toString()) - 1));
                        sb.setLength(0);
                        sb.append(c);
                        break;
                    }
                }
                
                if (i == template.length()) {
                    emitterList.add(new ParameterEmitter(Integer.valueOf(sb.toString()) - 1));
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        
        if (sb.length() > 0) {
            emitterList.add(new StringEmitter(sb.toString()));
        }
        
        this.emitters = emitterList.toArray(new TemplateEmitter[emitterList.size()]);
    }

    public String render(List<?> args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < emitters.length; i++) {
            emitters[i].emit(sb, args);
        }
        
        return sb.toString();
    }
}
