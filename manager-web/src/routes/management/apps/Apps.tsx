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

import React from 'react';
import MainLayout from '../../../views/mainLayout/MainLayout';
import AddButton from "../../../components/form/AddButton";
import {connect} from "react-redux";
import {ReduxState} from "../../../reducers";
import CardList from "../../../components/list/CardList";
import styles from './Apps.module.css'
import BaseComponent from "../../../components/BaseComponent";
import {loadApps} from "../../../actions";
import {IApp} from "./App";
import AppCard from "./AppCard";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    apps: IApp[];
}

interface DispatchToProps {
    loadApps: (name?: string) => any;
}

type Props = StateToProps & DispatchToProps;

class Apps extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadApps();
    }

    public render() {
        return (
            <MainLayout>
                <AddButton tooltip={{text: 'Add app', position: 'left'}}
                           pathname={'/apps/new_app?new=true'}/>
                <div className={`${styles.container}`}>
                    <CardList<IApp>
                        isLoading={this.props.isLoading}
                        error={this.props.error}
                        emptyMessage={"No apps to display"}
                        list={this.props.apps}
                        card={this.app}
                        predicate={this.predicate}/>
                </div>
            </MainLayout>
        );
    }

    private app = (app: IApp): JSX.Element =>
        <AppCard key={app.name} app={app}/>;

    private predicate = (app: IApp, search: string): boolean =>
        app.name.toString().toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.apps.isLoadingApps,
        error: state.entities.apps.loadAppsError,
        apps: (state.entities.apps.data && Object.values(state.entities.apps.data)) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadApps,
};

export default connect(mapStateToProps, mapDispatchToProps)(Apps);
