import List from "../../../components/list/List";
import React from "react";
import {ReduxState} from "../../../reducers";
import {loadLogs} from "../../../actions";
import {connect} from "react-redux";
import {ILogs} from "./ManagementLogs";
import ListItem from "../../../components/list/ListItem";
import styles from './LogsList.module.css';
import ActionButton from "../../../components/list/ActionButton";
import {capitalize} from "../../../utils/text";

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  logs: ILogs[];
}

interface DispatchToProps {
  loadLogs: () => void;
}

type Props = StateToProps & DispatchToProps;

class LogsList extends React.Component<Props, {}> {

  private reloadLogs: NodeJS.Timeout | null = null;

  public componentDidMount(): void {
    this.props.loadLogs();
  };

  componentWillUnmount(): void {
    if (this.reloadLogs) {
      clearTimeout(this.reloadLogs);
      this.reloadLogs = null;
    }
  }

  private predicate = (logs: ILogs, search: string): boolean =>
    logs.formattedMessage.toLowerCase().includes(search)
    || logs.levelString.toLowerCase().includes(search);

  private getLevelColor = (levelString: string) => {
    switch (levelString.toLowerCase()) {
      case 'trace': return 'grey-text';
      case 'debug': return 'green-text';
      case 'info': return 'blue-text';
      case 'warn': return 'yellow-text';
      case 'error': return 'red-text';
    }
  };

  private header = (): JSX.Element =>
    <ListItem>
      <div className={`${styles.headerItem}`}>
        <span className={`${styles.timestampColumn}`}>
          timestamp
        </span>
        <span className={`${styles.levelColumn}`}>
          level
        </span>
        <span className={`${styles.infoColumn}`}>
          message
        </span>
      </div>
    </ListItem>;

  private log = (log: ILogs, index: number, last: boolean): JSX.Element =>
    <ListItem>
      <div className={`${last ? styles.lastItem : styles.item}`}>
        <span className={`${styles.column}`}>
          {new Date(log.timestmp).toLocaleString()}
        </span>
        <span className={`${styles.column} ${this.getLevelColor(log.levelString)}`}>
          {capitalize(log.levelString.toLowerCase())}
        </span>
        <span className={`${styles.column}`}>
          {log.formattedMessage}
        </span>
      </div>
    </ListItem>;

  private onReloadClick = (): void => {
    if (this.reloadLogs) {
      clearTimeout(this.reloadLogs);
      this.reloadLogs = null;
    }
    else {
      this.reloadLogs = setInterval(this.props.loadLogs, 5000);
    }
  };

  public render() {
    const {error, logs} = this.props;
    let isLoading = this.props.isLoading;
    const LogsList = List<ILogs>();
    return (
      <>
        <ActionButton icon={'cached'}
                      tooltip={{activatedText: 'Deactivate automatic reload', deactivatedText: 'Activate automatic reload', position: 'bottom'}}
                      clickCallback={this.onReloadClick}
                      automatic/>
        <div className={`${styles.container} ${!isLoading && !error ? styles.list : undefined}`}>
          <LogsList
            isLoading={isLoading}
            error={error}
            emptyMessage={'No logs to show'}
            list={logs}
            show={this.log}
            paginate={{pagesize: { initial: 25, options: [5, 10, 25, 50, 100, 'all'] }, page: { last: true }, position: 'top-bottom'}}
            predicate={this.predicate}
            header={this.header}/>
        </div>
      </>
    );
  }

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    isLoading: state.entities.logs.isLoadingLogs,
    error: state.entities.logs.loadLogsError,
    logs: (state.entities.logs && Object.values(state.entities.logs.data)) || [],
  }
);

const mapDispatchToProps: DispatchToProps = {
  loadLogs
};

export default connect(mapStateToProps, mapDispatchToProps)(LogsList)