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
