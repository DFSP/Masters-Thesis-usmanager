/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pt.unl.fct.miei.usmanagement.manager.worker.events;

import java.io.Serializable;
import java.lang.reflect.Type;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;

@Slf4j
public class AuditInterceptor extends EmptyInterceptor {

  private int updates;
  private int creates;
  private int loads;

  public void onDelete(Object entity,
                       Serializable id,
                       Object[] state,
                       String[] propertyNames,
                       Type[] types) {
    // do nothing
  }

  public boolean onFlushDirty(Object entity,
                              Serializable id,
                              Object[] currentState,
                              Object[] previousState,
                              String[] propertyNames,
                              Type[] types) {
    /*if ( entity instanceof Auditable ) {
      updates++;
      for ( int i=0; i < propertyNames.length; i++ ) {
        if ( "lastUpdateTimestamp".equals( propertyNames[i] ) ) {
          currentState[i] = new Date();
          return true;
        }
      }
    }*/
    log.info("{}", entity);
    return false;
  }

  public boolean onLoad(Object entity,
                        Serializable id,
                        Object[] state,
                        String[] propertyNames,
                        Type[] types) {
    /*if ( entity instanceof Auditable ) {
      loads++;
    }*/
    log.info("{}", entity);
    return false;
  }

  public boolean onSave(Object entity,
                        Serializable id,
                        Object[] state,
                        String[] propertyNames,
                        Type[] types) {

    /*if ( entity instanceof Auditable ) {
      creates++;
      for ( int i=0; i<propertyNames.length; i++ ) {
        if ( "createTimestamp".equals( propertyNames[i] ) ) {
          state[i] = new Date();
          return true;
        }
      }
    }*/
    log.info("{}", entity);
    return false;
  }

  public void afterTransactionCompletion(Transaction tx) {
    /*if ( tx.wasCommitted() ) {
      System.out.println("Creations: " + creates + ", Updates: " + updates, "Loads: " + loads);
    }
    updates=0;
    creates=0;
    loads=0;*/
  }

}
