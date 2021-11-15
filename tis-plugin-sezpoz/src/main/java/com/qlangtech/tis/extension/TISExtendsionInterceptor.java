/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.extension;

import net.java.sezpoz.Indexable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * 通过编译时期拦截扩展插件接口，将一些插件的元信息写入到 class compiler output目录当中去
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-11-12 09:24
 **/
@SupportedAnnotationTypes("*")
@SupportedOptions("sezpoz.quiet")
public class TISExtendsionInterceptor extends AbstractProcessor {
    public TISExtendsionInterceptor() {
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (env.processingOver()) {
            // TODO we should not write until processingOver
            return false;
        }
//        for (Element indexable : env.getElementsAnnotatedWith(Indexable.class)) {
//            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING
//                    , "element:" + indexable.getSimpleName() + "," + indexable.getSimpleName());
//        }
//        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "kkkkkk---------------------------------");
//
//        for (TypeElement te : annotations) {
//            for (Element e : env.getElementsAnnotatedWith(te)) {
//                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "print:" + e.toString());
//            }
//        }

//        for (Element indexable : roundEnv.getElementsAnnotatedWith(Indexable.class)) {
//            String error = verifyIndexable(indexable);
//            if (error != null) {
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error, indexable);
//            } else {
//                Retention retention = indexable.getAnnotation(Retention.class);
//                if (retention == null || retention.value() != RetentionPolicy.SOURCE) {
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "should be marked @Retention(RetentionPolicy.SOURCE)", indexable);
//                }
//            }
//        }
//        // map from indexable annotation names, to actual uses
//        Map<String, Map<String, SerAnnotatedElement>> output = new TreeMap<String,Map<String,SerAnnotatedElement>>();
//        Map<String, Collection<Element>> originatingElementsByAnn = new HashMap<String,Collection<Element>>();
//        scan(annotations, originatingElementsByAnn, roundEnv, output);
//        write(output, originatingElementsByAnn);
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

}
