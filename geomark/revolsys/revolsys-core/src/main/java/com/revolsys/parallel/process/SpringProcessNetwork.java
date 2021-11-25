package com.revolsys.parallel.process;

import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.logging.Logs;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.revolsys.spring.TargetBeanFactoryBean;
import com.revolsys.spring.TargetBeanProcess;

public class SpringProcessNetwork extends ProcessNetwork
  implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

  @Override
  public void onApplicationEvent(final ContextRefreshedEvent event) {
    if (isAutoStart()) {
      start();
    }
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean, final String beanName)
    throws BeansException {
    final ProcessNetwork parent = getParent();
    if (parent == null) {
      if (bean instanceof TargetBeanFactoryBean) {
        final TargetBeanFactoryBean targetBean = (TargetBeanFactoryBean)bean;
        final Class<?> targetClass = targetBean.getObjectType();
        if (Process.class.isAssignableFrom(targetClass)) {
          try {
            final Process process = new TargetBeanProcess(targetBean);
            addProcess(process);
          } catch (final Exception e) {
            Logs.error(this, "Unable to create process for bean " + beanName, e);
          }

        }
      } else if (bean instanceof Process) {
        final Map<Process, Thread> processes = getProcessMap();
        final Process process = (Process)bean;
        // Check to see if this was a target bean, if so make sure duplicate
        // threads aren't created
        if (processes != null) {
          for (final Entry<Process, Thread> entry : processes.entrySet()) {
            final Process otherProcess = entry.getKey();
            if (otherProcess instanceof TargetBeanProcess) {
              final TargetBeanProcess targetProcessBean = (TargetBeanProcess)otherProcess;
              if (targetProcessBean.isInstanceCreated()) {
                final Process targetProcess = targetProcessBean.getProcess();
                if (targetProcess == process) {
                  synchronized (getSync()) {
                    final Thread thread = entry.getValue();
                    processes.put(targetProcess, thread);
                    processes.remove(otherProcess);
                    return bean;
                  }
                }
              }
            }
          }
        }
        addProcess(process);
      }
    } else if (parent instanceof SpringProcessNetwork) {
      final SpringProcessNetwork parentProcessNetwork = (SpringProcessNetwork)parent;
      parentProcessNetwork.postProcessAfterInitialization(bean, beanName);
    }
    return bean;
  }

  @Override
  public Object postProcessBeforeInitialization(final Object bean, final String beanName)
    throws BeansException {
    return bean;
  }
}
