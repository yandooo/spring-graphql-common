/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 oEmbedler Inc. and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 *  persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.oembedler.moon.graphql;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.NestedRuntimeException;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class SpringGraphQLSchemaBeanFactory implements ApplicationContextAware, GraphQLSchemaBeanFactory {

    private ApplicationContext applicationContext;

    @Override
    public <T> T getBeanByType(final Class<T> objectClass) {
        return retrieveBean(objectClass, null);
    }

    @Override
    public <T> T getBeanByName(final String beanName) {
        return retrieveBean(null, beanName);
    }

    @Override
    public <T> boolean containsBean(final Class<T> objectClass) {
        String[] beanNames = applicationContext.getBeanNamesForType(objectClass);
        return beanNames != null && beanNames.length > 0;
    }

    private <T> T retrieveBean(final Class<T> objectClass, final String beanName) {
        T object = null;
        if (StringUtils.isNoneBlank(beanName) &&
                applicationContext != null && applicationContext.containsBean(beanName)) {
            object = (T) applicationContext.getBean(beanName);
        } else {
            object = BeanFactoryUtils.beanOfType(applicationContext, objectClass);
        }
        return object;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
