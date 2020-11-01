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

import BaseComponent from "../../../components/BaseComponent";
import {RouteComponentProps} from "react-router";
import Form from "../../../components/form/Form";
import Field from "../../../components/form/Field";
import LoadingSpinner from "../../../components/list/LoadingSpinner";
import {Error} from "../../../components/errors/Error";
import React from "react";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {loadRegions} from "../../../actions";
import {connect} from "react-redux";
import {isNew} from "../../../utils/router";

export interface IRegion /*extends IDatabaseData*/
{
    region: string;
    coordinates: Coordinates;
    /*active: boolean;*/
}

const buildNewRegion = (): Partial<IRegion> => ({
    region: undefined,
    coordinates: undefined,
    /*active: true,*/
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    region: Partial<IRegion>;
    /*formRegion?: Partial<IRegion>,*/
}

interface DispatchToProps {
    loadRegions: (name: string) => void;
    /*addRegion: (region: IRegion) => void;
    updateRegion: (previousRegion: IRegion, currentRegion: IRegion) => void;*/
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: IRegion,
    selected: 'region'
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    region?: IRegion,
    /*formRegion?: IRegion,*/
}

class Region extends BaseComponent<Props, State> {

    state: State = {};
    private mounted = false;

    public componentDidMount(): void {
        this.loadRegion();
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    public render() {
        return (
            <MainLayout>
                <div className="container">
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private loadRegion = () => {
        if (!isNew(this.props.location.search)) {
            const regionName = this.props.match.params.name;
            this.props.loadRegions(regionName);
        }
    };

    private getRegion = () =>
        this.state.region || this.props.region;

    /*private getFormRegion = () =>
        this.state.formRegion || this.props.formRegion;*/

    /*private isNew = () =>
        isNew(this.props.location.search);*/

    /*private onPostSuccess = (reply: IReply<IRegion>): void => {
        const region = reply.data;
        super.toast(`<span class="green-text">Region ${this.mounted ? `<b class="white-text">${region.name}</b>` : `<a href=/regions/${region.name}><b>${region.name}</b></a>`} saved</span>`);
        this.props.addRegion(region);
        if (this.mounted) {
            this.updateRegion(region);
            this.props.history.replace(region.name);
        }
    };*/

    /*private onPostFailure = (reason: string, region: IRegion): void =>
        super.toast(`Unable to save region <b>${region.name}</b>`, 10000, reason, true);*/

    /*private onPutSuccess = (reply: IReply<IRegion>): void => {
        const region = reply.data;
        super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${region.name}</b>` : `<a href=/regions/${region.name}><b>${region.name}</b></a>`} region have been saved</span>`);
        const previousRegion = this.getRegion();
        if (previousRegion?.id) {
            this.props.updateRegion(previousRegion as IRegion, region)
        }
        if (this.mounted) {
            this.updateRegion(region);
            this.props.history.replace(region.name);
        }
    };*/

    /*private onPutFailure = (reason: string, region: IRegion): void =>
        super.toast(`Unable to update region ${this.mounted ? `<b>${region.name}</b>` : `<a href=/regions/${region.name}><b>${region.name}</b></a>`}`, 10000, reason, true);*/

    /*private onDeleteSuccess = (region: IRegion): void => {
        super.toast(`<span class="green-text">Region <b class="white-text">${region.name}</b> successfully removed</span>`);
        if (this.mounted) {
            this.props.history.push(`/regions`);
        }
    };*/

    /*private onDeleteFailure = (reason: string, region: IRegion): void =>
        super.toast(`Unable to delete region ${this.mounted ? `<b>${region.name}</b>` : `<a href=/regions/${region.name}><b>${region.name}</b></a>`}`, 10000, reason, true);*/

    /*private updateRegion = (region: IRegion) => {
        region = Object.values(normalize(region, Schemas.REGION).entities.regions || {})[0];
        const formRegion = {...region};
        removeFields(formRegion);
        this.setState({region: region, formRegion: formRegion});
    };*/

    /*private getFields = (region: Partial<IRegion>): IFields =>
        Object.keys(region).map(key => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: key === 'coordinates'
                        ? {rule: requireGreaterOrEqualSize, args: 1}
                        : {rule: required}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});*/

    private region = () => {
        const {isLoading, error} = this.props;
        const region = this.getRegion();
        /*const formRegion = this.getFormRegion();*/
        // @ts-ignore
        const regionKey: (keyof IRegion) = region && Object.keys(region)[0];
        /*const isNewRegion = this.isNew();*/
        return (
            <>
                {/*{!isNewRegion && isLoading && <LoadingSpinner/>}*/}
                {isLoading && <LoadingSpinner/>}
                {/*{!isNewRegion && !isLoading && error && <Error message={error}/>}*/}
                {!isLoading && error && <Error message={error}/>}
                {/*{(isNewRegion || !isLoading) && (isNewRegion || !error) && formRegion && (*/}
                {!isLoading && !error && region && (
                    /*@ts-ignore*/
                    <Form id={regionKey}
                        /*fields={this.getFields(formRegion)}*/
                        /*fields={this.getFields(region)}*/
                          values={region}
                        /*isNew={isNew(this.props.location.search)}*/
                        /*post={{
                            url: 'regions',
                            successCallback: this.onPostSuccess,
                            failureCallback: this.onPostFailure
                        }}*/
                        /*put={{
                            url: `regions/${region.name}`,
                            successCallback: this.onPutSuccess,
                            failureCallback: this.onPutFailure
                        }}*/
                        /*delete={{
                            url: `regions/${region.name}`,
                            successCallback: this.onDeleteSuccess,
                            failureCallback: this.onDeleteFailure
                        }}*/>
                        {Object.keys(region).map((key, index) =>
                            /*key === 'active'
                                ? <Field key={index}
                                                     id={key}
                                                     type='checkbox'
                                                     checkbox={{label: 'region active'}}/>
                                :*/ key === 'coordinates'
                            ? <Field key={index} id='coordinates' label='location' type='map'
                                     map={{loading: isLoading, editable: false, zoomable: true, labeled: true}}/>
                            : <Field key={index}
                                     id={key}
                                     label={key}/>
                        )}
                    </Form>
                )}
            </>
        )
    };

    private tabs = (): Tab[] => [
        {
            title: 'Region',
            id: 'region',
            content: () => this.region(),
            active: this.props.location.state?.selected === 'region'
        },
    ];

}

/*function removeFields(region: Partial<IRegion>) {
    delete region["id"];
}*/

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.regions.isLoadingRegions;
    const error = state.entities.regions.loadRegionsError;
    const name = props.match.params.name;
    const region = isNew(props.location.search) ? buildNewRegion() : state.entities.regions.data[name];
    /*let formRegion;
    if (region) {
        formRegion = {...region};
        removeFields(formRegion);
    }*/
    return {
        isLoading,
        error,
        region,
        /*formRegion,*/
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadRegions,
    /*addRegion,
    updateRegion,*/
};

export default connect(mapStateToProps, mapDispatchToProps)(Region);