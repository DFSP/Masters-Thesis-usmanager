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

import React from "react";
import MainLayout from "../../../views/mainLayout/MainLayout";
import styles from './Settings.module.css'
import PerfectScrollbar from "react-perfect-scrollbar";
import Field from "../../../components/form/Field";
import Form, {required} from "../../../components/form/Form";
import BaseComponent from "../../../components/BaseComponent";

type FormOption = 'initialization' | 'monitoring' | 'timeouts' | 'parallelism'

interface State {
    selectedForm: FormOption
}

class Settings extends BaseComponent<{}, State> {

    constructor(props: {}) {
        super(props);
        this.state = {
            selectedForm: "initialization"
        }
    }

    private switchForm = (selected: FormOption) => (_: any) => {
        this.setState({selectedForm: selected})
    }

    private initFields = () => ({
        ['initial-max-workers']: {
            id: 'initial-max-workers',
            label: 'initial-max-workers',
        }
    })

    private monitoringFields = () => ({

    })

    private timeoutsFields = () => ({

    })

    private parallelismFields = () => ({

    })

    private getSelectedForm = (): JSX.Element | undefined => {
        switch (this.state.selectedForm) {
            case "initialization":
                return (
                    // @ts-ignore
                    <Form id={'form'}
                          fields={this.initFields()}
                          values={{'initial-max-workers': 1}}
                          isNew={true}
                          controlsMode={"none"}>
                        <Field id={'initial-max-workers'}
                               type={'number'}
                               label={'initial-max-workers'}
                               validation={false}
                               icon={{include: false}}/>
                    </Form>
                );
            case "monitoring":
                return (
                    // @ts-ignore
                    <Form id={'form'}
                          fields={this.monitoringFields()}
                          values={{'initial-max-workers': 1}}
                          isNew={true}
                          controlsMode={"none"}>

                    </Form>
                );
            case "timeouts":
                return (
                    // @ts-ignore
                    <Form id={'form'}
                          fields={this.timeoutsFields()}
                          values={{'initial-max-workers': 1}}
                          isNew={true}
                          controlsMode={"none"}>

                    </Form>
                );
            case "parallelism":
                return (
                    // @ts-ignore
                    <Form id={'form'}
                          fields={this.parallelismFields()}
                          values={{'initial-max-workers': 1}}
                          isNew={true}
                          controlsMode={"none"}>

                    </Form>
                );
        }

    }

    render() {
        const {selectedForm} = this.state;
        return <MainLayout>
            <div className='row col s12 m12'>
                <div className={`${styles.container}`}>
                    <PerfectScrollbar>
                        <ul className={styles.optionsContainer}>
                            <li className={`${selectedForm === 'initialization' ? styles.optionSelected : styles.option}`}
                                aria-selected={selectedForm === 'initialization'}
                                onClick={this.switchForm('initialization')}>
                                Inicialização
                            </li>
                            <li className={`${selectedForm === 'monitoring' ? styles.optionSelected : styles.option}`}
                                aria-selected={selectedForm === 'monitoring'}
                                onClick={this.switchForm('monitoring')}>
                                Monitorização
                            </li>
                            <li className={`${selectedForm === 'timeouts' ? styles.optionSelected : styles.option}`}
                                aria-selected={selectedForm === 'timeouts'}
                                onClick={this.switchForm('timeouts')}>
                                Timeouts
                            </li>
                            <li className={`${selectedForm === 'parallelism' ? styles.optionSelected : styles.option}`}
                                aria-selected={selectedForm === 'parallelism'}
                                onClick={this.switchForm('parallelism')}>
                                Paralelismo
                            </li>
                        </ul>
                    </PerfectScrollbar>
                    <div className={styles.settings}>
                        {this.getSelectedForm()}
                        <button className={`btn-flat btn-small green-text slide inline-button ${styles.saveButton}`}>
                            Guardar
                        </button>
                    </div>
                </div>
            </div>

        </MainLayout>
    }

}

export default Settings;