package com.example.demo.Behavior;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("serial")
public class MyParallelMultiInstanceBehavior extends ParallelMultiInstanceBehavior {

    public MyParallelMultiInstanceBehavior(ActivityImpl activity, AbstractBpmnActivityBehavior originalActivityBehavior) {
        super(activity, originalActivityBehavior);
    }
    @SuppressWarnings("rawtypes")
	public void myexecuteOriginalBehavior(ActivityExecution execution, int loopCounter) throws Exception {
        if (usesCollection() && collectionElementVariable != null) {
            Collection collection = null;
            if (collectionExpression != null) {
                collection = (Collection) collectionExpression.getValue(execution);
            } else if (collectionVariable != null) {
                collection = (Collection) execution.getVariable(collectionVariable);
            }

            Object value = null;
            int index = 0;
            Iterator it = collection.iterator();
            while (index <= loopCounter) {
                value = it.next();
                index++;
            }
            setLoopVariable(execution, collectionElementVariable, value);
        }
        // If loopcounter == 1, then historic activity instance already created, no need to
        // pass through executeActivity again since it will create a new historic activity
        if (loopCounter == 0) {
            callCustomActivityStartListeners(execution);
            innerActivityBehavior.execute(execution);
        } else {
            execution.executeActivity(activity);
        }
    }
}
