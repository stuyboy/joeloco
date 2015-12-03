var MessageBox = React.createClass({

    getInitialState: function () {
        this.messageText = "";
        return {messageText: ""};
    },

    render: function () {
        return (
            <div id="messageBox" className="chatBox">
            <form onSubmit={ this.handleSubmit }>
                <input className="textSubmit" type="submit" value="Send"/>
                <div className="textFieldDiv">
                <input className="textField" onChange={ this.changeHandler } value={ this.state.text } />
                </div>
            </form>
            </div>
        );
    },

    changeHandler: function(e) {
        this.setState({ text: e.target.value });
    },

    handleSubmit: function(e) {
        //prevent submit from submitting.
        e.preventDefault();
        var msg = {
            name: "Web User",
            message: this.state.text
        }
        this.props.submitAction(msg);
        this.setState({text: ""});
    }
});

var User = {
    DEFAULT: { userId: 'Web User', img: "/img/webuseravatar.png" },
    userList: {},
    findUser: function(pUserId) {
        if (typeof(this.userList[pUserId]) !== "undefined") {
            return this.userList[pUserId];
        }
        var user = {
            userId: pUserId,
            img: null,
            class: null
        };

        if (pUserId == this.DEFAULT.userId) {
            user = this.DEFAULT;
        } else {
            var avatarUrl = 'http://joelo.co:8080/users/' + pUserId + '/avatar?sz=50&sp=CIRCLE';
            user.img = avatarUrl;
        }

        if (Object.keys(this.userList).length % 2 == 0) {
            user.class = "triangle-right right";
        } else {
            user.class = "triangle-right left";
        }

        this.userList[pUserId] = user;
        return user;
    }
};

var MessageList = React.createClass({
        render: function() {
            var createItem = function(item) {
                var userObj;
                if (typeof(item.userId) === 'undefined') {
                    userObj = User.findUser(item.name);
                } else {
                    userObj = User.findUser(item.userId);
                }

                return (
                    <div className={ userObj.class }>
                    <div className="avatar"><img src={ userObj.img }/></div>
                    <div className="msgBody">{ item.message }</div>
                    <div className="msgFrom">{ item.name }</div>
                    </div>
                );
            };

            return (
                <div id="messageList" className="chat">
                { this.props.messages.map(createItem) }
                </div>
            );
        }
});

var ChatApp = React.createClass({
    getInitialState: function() {
        this.messages = [];
        return {messages: [], text: ""};
    },

    componentWillMount: function() {
        this.firebaseRef = new Firebase(this.props.url);
        this.firebaseRef.on('child_added', function(snapshot) {
            this.messages.push(snapshot.val());
            this.setState({
                messages: this.messages
            });
        }.bind(this));
    },

    componentWillUnmount: function() {
        this.firebaseRef.off();
    },

    submitMessage: function(msgObject) {
        this.firebaseRef.push( msgObject );
    },

    componentWillUpdate: function() {
        var node = React.findDOMNode(this.refs.messageList);
        this.shouldScrollBottom = node.scrollTop + node.offsetHeight === node.scrollHeight;
    },

    componentDidUpdate: function() {
        if (this.shouldScrollBottom) {
            var node = React.findDOMNode(this.refs.messageList);
            this.scrollTo(node, node.scrollHeight, 2000);
        }
    },

    render: function() {
        return (
            <div>
                <MessageList ref="messageList" messages={ this.state.messages } />
                <MessageBox submitAction={ this.submitMessage } />
            </div>
        )
    },

    scrollTo: function(element, to, duration) {
        var start = element.scrollTop,
            change = to - start,
            currentTime = 0,
            increment = 20;

        var animateScroll = function() {
            currentTime += increment;
            var val = easeInOutQuad(currentTime, start, change, duration);
            element.scrollTop = val;
            if (currentTime < duration) {
                setTimeout(animateScroll, increment);
            }
        };

        var easeInOutQuad = function(t, b, c, d) {
                t /= d/2;
                if (t < 1) return c/2*t*t + b;
                t--;
                return -c/2 * (t*(t-2) - 1) + b;
        };

        animateScroll();
    }

});

