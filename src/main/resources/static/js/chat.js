
var statusApp = new Vue({
    el:'#chatApp',
    data: {
        actorStatuses: [      ]
        , chatBody: ""
        , chatStatus: ""
        , personName: ""
        , message: ""
    },
    created: function () {
        this.getData();
        this.setDataRefreshInterval();
    },
    methods: {
        setData: function(data){
            this.actorStatuses = data.actorStatuses;
            this.chatBody = data.chatBody;
            this.chatStatus = data.chatStatus;
        }
        , setDataAndRefreshInterval: function(data){
            this.setData(data);
            this.setDataRefreshInterval();
        }
        , getData: function() {
            var that = this;
            axios.get('/actorStatuses').then(function(response) {
                that.setData(response.data);
            }) ;
        }
        , setDataRefreshInterval: function(){
            if(this.dtaInterval){
                clearInterval(this.dtaInterval);
            }
            var that = this;
            this.dtaInterval = setInterval(function() {
                that.getData();
            }, 2000);
        }
        , login: function(){
            var that = this;
            axios.get('/newPerson/' +this.personName).then(function(response) {
                that.setDataAndRefreshInterval(response.data);
            });
        }
        , logout: function(){
            var that = this;
            axios.get('/logout/' +this.personName).then(function(response) {
                that.setDataAndRefreshInterval(response.data);
            });
        }
        , writeMessage: function(){
            var that = this;
            axios.get('/newMessage/' +this.personName + "?message="+this.message).then(function(response) {
                that.setDataAndRefreshInterval(response.data);
            });
        }
    }

});