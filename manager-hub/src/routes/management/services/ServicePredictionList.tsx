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
import {IService} from "./Service";
import IDatabaseData from "../../../components/IDatabaseData";
import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {loadServicePredictions, removeServicePredictions} from "../../../actions";
import {connect} from "react-redux";
import {IFields, IValues, requiredAndNumberAndMin, requiredAndTrimmed} from "../../../components/form/Form";
import Field, {getTypeFromValue} from "../../../components/form/Field";

export interface IPrediction extends IDatabaseData {
    name: string;
    description: string;
    startDate: string;
    startTime: string;
    endDate: string;
    endTime: string;
    minimumReplicas: number;
}

const emptyPrediction = (): Partial<IPrediction> => ({
    name: undefined,
    description: undefined,
    startDate: undefined,
    startTime: undefined,
    endDate: undefined,
    endTime: undefined,
    minimumReplicas: undefined
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    predictions: IPrediction[];
}

interface DispatchToProps {
    loadServicePredictions: (serviceName: string) => void;
    removeServicePredictions: (serviceName: string, predictions: string[]) => void;
}

interface ServicePredictionListProps {
    isLoadingService: boolean;
    loadServiceError?: string | null;
    service: IService | Partial<IService> | null;
    unsavedPredictions: IPrediction[];
    onAddServicePrediction: (prediction: IPrediction) => void;
    onRemoveServicePredictions: (prediction: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ServicePredictionListProps;

interface State {
    entitySaved: boolean;
}

class ServicePredictionList extends BaseComponent<Props, State> {

    //TODO allow to edit prediction details, by clicking the prediction opening a filled modal form

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.service?.serviceName && this.props.service?.serviceName) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList<IPrediction>
            isLoading={!isNew ? this.props.isLoadingService || this.props.isLoading : undefined}
            error={!isNew ? this.props.loadServiceError || this.props.error : undefined}
            emptyMessage='Predictions list is empty'
            data={this.props.predictions}
            dataKey={['name']}
            formModal={{
                id: 'servicePrediction',
                title: 'Add prediction',
                fields: this.getFields(),
                values: emptyPrediction(),
                content: this.predictionModal,
                fullScreen: window.innerWidth < 993,
            }}
            show={this.prediction}
            onAddInput={this.onAdd}
            onRemove={this.onRemove}
            onDelete={{
                url: `services/${this.props.service?.serviceName}/predictions`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}
            entitySaved={this.state.entitySaved}/>;

    }

    private loadEntities = () => {
        if (this.props.service?.serviceName) {
            const {serviceName} = this.props.service;
            this.props.loadServicePredictions(serviceName);
        }
    };

    private isNew = () =>
        this.props.service?.serviceName === undefined;

    private prediction = (index: number, prediction: IPrediction, separate: boolean, checked: boolean,
                          handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unsavedPredictions.includes(prediction);
        return (
            <ListItem key={index} separate={separate}>
                <div className={`${styles.listItemContent}`}>
                    <label>
                        <input id={prediction.name}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
              <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                {prediction.name} ({prediction.minimumReplicas} replicas)
              </div>
            </span>
                    </label>
                    <div className={`${styles.smallText}`}>
                        {prediction.startDate === prediction.endDate ?
                            <div>{prediction.startDate} {prediction.startTime} <span
                                className={styles.arrow}>&rarr;</span> {prediction.endTime}</div>
                            :
                            <>
                                <div>{prediction.startDate} {prediction.startTime}</div>
                                <div>{prediction.endDate} {prediction.endTime}</div>
                            </>
                        }
                    </div>
                </div>
            </ListItem>
        );
    };

    private onAdd = (prediction: IValues): void => {
        this.props.onAddServicePrediction(prediction as IPrediction);
    };

    private onRemove = (predictions: string[]) =>
        this.props.onRemoveServicePredictions(predictions);

    private onDeleteSuccess = (predictions: string[]): void => {
        if (this.props.service?.serviceName) {
            const {serviceName} = this.props.service;
            this.props.removeServicePredictions(serviceName, predictions);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Unable to delete prediction`, 10000, reason, true);

    private getFields = (): IFields =>
        Object.entries(emptyPrediction()).map(([key, value]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: getTypeFromValue(value) === 'number'
                        ? {rule: requiredAndNumberAndMin, args: 0}
                        : {rule: requiredAndTrimmed}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private predictionModal = () =>
        <div>
            <Field key='name' id={'name'} label='name' disabled={false}/>
            <Field key='description' id={'description'} label='description' type='multilinetext'/>
            <div className={'col s6 inline-field'}>
                <Field key='startDate' id={'startDate'} label='startDate' type='datepicker'/>
            </div>
            <div className={'col s6 inline-field'}>
                <Field key='startTime' id={'startTime'} label='startTime' type='timepicker' icon={{include: false}}/>
            </div>
            <div className={'col s6 inline-field'}>
                <Field key='endDate' id={'endDate'} label='endDate' type='datepicker'/>
            </div>
            <div className={'col s6 inline-field'}>
                <Field key='endTime' id={'endTime'} label='endTime' type='timepicker' icon={{include: false}}/>
            </div>
            <Field key='minimumReplicas' id={'minimumReplicas'} label='minimumReplicas' type={'number'}/>
        </div>;

}

function mapStateToProps(state: ReduxState, ownProps: ServicePredictionListProps): StateToProps {
    const serviceName = ownProps.service?.serviceName;
    const service = serviceName && state.entities.services.data[serviceName];
    const predictions = service && service.predictions;
    return {
        isLoading: state.entities.services.isLoadingPredictions,
        error: state.entities.services.loadPredictionsError,
        predictions: (predictions && Object.values(predictions)) || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadServicePredictions,
        removeServicePredictions,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ServicePredictionList);