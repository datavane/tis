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

import net.java.sezpoz.impl.Indexer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * 通过编译时期拦截扩展插件接口，将一些插件的元信息写入到 class compiler output目录当中去
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-11-12 09:24
 **/
@SupportedAnnotationTypes("*")
@SupportedOptions("sezpoz.quiet")
public class TISExtendsionInterceptor extends AbstractProcessor {
    public static final String FILE_EXTENDPOINTS = "extendpoints.txt";

    public TISExtendsionInterceptor() {
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (env.processingOver()) {
            // TODO we should not write until processingOver`
            return false;
        }

        Map<String, List<String>> extensionPoints = new HashMap<>();
        for (Element indexable : env.getElementsAnnotatedWith(TISExtension.class)) {
            Element enclose = indexable.getEnclosingElement();
            visitParent(extensionPoints, enclose, enclose.asType());
        }

        try {
            FileObject out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                    "", Indexer.METAINF_ANNOTATIONS + FILE_EXTENDPOINTS
            );
            try (OutputStream o = out.openOutputStream()) {

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        for (Map.Entry<String, List<String>> entry : extensionPoints.entrySet()) {
//            System.out.println(entry.getKey() + "->" + entry.getValue().stream().collect(Collectors.joining(",")));
//        }


//        for (TypeElement te : annotations) {
//            //Element enclosingElement = te.getEnclosingElement();
//            System.out.println("enclosingElement:" + te);
//            for (Element e : env.getElementsAnnotatedWith(te)) {
//                System.out.println("=====print:" + e.toString());
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

    private void visitParent(Map<String, List<String>> extensionPoints, final Element impl, TypeMirror childtm) {
        if (childtm.getKind() != TypeKind.DECLARED) {
            return;
        }
        Types typeUtils = this.processingEnv.getTypeUtils();
        String extendPoint = childtm.toString();
        TypeVisitor typeVisitor = null;
        List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(childtm);
        List<String> impls = null;
        for (TypeMirror tm : typeMirrors) {
//com.qlangtech.tis.extension.Describable<com.qlangtech.tis.plugin.ds.DataSourceFactory>,
            typeVisitor = new TypeVisitor();
            tm.accept(typeVisitor, null);
            if (typeVisitor.extendPointMatch) {
                impls = extensionPoints.get(extendPoint);
                if (impls == null) {
                    impls = new ArrayList<>();
                    extensionPoints.put(extendPoint, impls);
                }
                impls.add(String.valueOf(impl));
                return;
            }
            visitParent(extensionPoints, impl, tm);
        }
    }

    private static class TypeVisitor extends SimpleTypeVisitor8<Void, Void> {
        private boolean extendPointMatch;
        String extendPoint;

        public TypeVisitor() {
        }

        @Override
        public Void visitDeclared(DeclaredType t, Void aVoid) {
            extendPointMatch = t.asElement().getSimpleName().contentEquals("Describable");
//            if (extendPointMatch) {
//                System.out.println(t + "----------->extendPointMatch:" + extendPointMatch + "t.getTypeArguments() size:" + t.getTypeArguments().size());
//            }

            if (extendPointMatch) {
                for (TypeMirror p : t.getTypeArguments()) {
                    extendPoint = String.valueOf(p);

                }
            }

            return null;
        }

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

}
