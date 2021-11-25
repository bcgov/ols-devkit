/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolsys.beans.propertyeditor;

import javax.xml.namespace.QName;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.revolsys.spring.resource.Resource;

public class ResourceEditorRegistrar implements PropertyEditorRegistrar, BeanFactoryPostProcessor {
  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    beanFactory.addPropertyEditorRegistrar(this);
  }

  @Override
  public void registerCustomEditors(final PropertyEditorRegistry registry) {
    registry.registerCustomEditor(QName.class, new QNameEditor());
    registry.registerCustomEditor(Boolean.class, new BooleanEditor());
    registry.registerCustomEditor(Resource.class, new ResourceEditor());
    registry.registerCustomEditor(PathName.class, new PathNameEditor());
    registry.registerCustomEditor(Identifier.class, new IdentifierEditor());
  }
}
