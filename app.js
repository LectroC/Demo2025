(function(){
  'use strict';

  angular.module('SnippetApp', ['ngRoute'])
    .run(['$rootScope', '$location', function($rootScope, $location){
      // Check login state on app start
      $rootScope.isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
      $rootScope.userName = localStorage.getItem('userName') || '';
      
      // Update navbar based on login state
      function updateNavbar() {
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        var loginLink = document.getElementById('loginLink');
        var registerLink = document.getElementById('registerLink');
        var guestLink = document.getElementById('guestLink');
        var signOutLink = document.getElementById('signOutLink');
        
        if (isLoggedIn) {
          if (loginLink) loginLink.style.display = 'none';
          if (registerLink) registerLink.style.display = 'none';
          if (guestLink) guestLink.style.display = 'none';
          if (signOutLink) signOutLink.style.display = 'inline-block';
        } else {
          if (loginLink) loginLink.style.display = 'inline-block';
          if (registerLink) registerLink.style.display = 'inline-block';
          if (guestLink) guestLink.style.display = 'inline-block';
          if (signOutLink) signOutLink.style.display = 'none';
        }
      }
      
      // Update navbar on route change
      $rootScope.$on('$routeChangeSuccess', updateNavbar);
      updateNavbar();

      // global signout for navbar link
      window.signOut = function(){
        var name = localStorage.getItem('userName') || '';
        localStorage.removeItem('isLoggedIn');
        localStorage.removeItem('userName');
        alert(name ? (name + ' signed out successfully') : 'Signed out successfully');
        $rootScope.$applyAsync(function(){
          $location.path('/welcome');
        });
      };
    }])
    .config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider){
      // Ensure routes work with "#/path" instead of "#!/path"
      $locationProvider.hashPrefix('');
      $routeProvider
        .when('/welcome', { templateUrl: 'tpl/welcome.html', controller: 'WelcomeCtrl', controllerAs: 'vm' })
        .when('/login', { templateUrl: 'tpl/login.html', controller: 'LoginCtrl', controllerAs: 'vm' })
        .when('/register', { templateUrl: 'tpl/register.html', controller: 'RegisterCtrl', controllerAs: 'vm' })
        .when('/app', { templateUrl: 'tpl/app.html', controller: 'MainCtrl', controllerAs: 'vm' })
        .otherwise({ redirectTo: '/welcome' });
    }])
    .controller('WelcomeCtrl', ['$timeout', function($timeout){
      var vm = this;
      
      // Initialize Owl Carousel after view loads
      $timeout(function(){
        if (typeof $ !== 'undefined' && $.fn.owlCarousel) {
          // Destroy existing carousel if it exists
          $('#featureCarousel').trigger('destroy.owl.carousel');
          
          var carousel = $('#featureCarousel').owlCarousel({
            items: 1,
            loop: true,
            autoplay: true,
            autoplayTimeout: 7000,
            autoplayHoverPause: true,
            nav: false,
            dots: true,
            center: true,
            margin: 20,
            stagePadding: 50,
            responsive: {
              0: { items: 1, center: true, stagePadding: 20 },
              768: { items: 2, center: false, stagePadding: 30 },
              1024: { items: 3, center: false, stagePadding: 50 }
            }
          });
          
          // Add click handlers for navigation buttons
          $('#prevBtn').click(function() {
            carousel.trigger('prev.owl.carousel');
          });
          
          $('#nextBtn').click(function() {
            carousel.trigger('next.owl.carousel');
          });
          
          // Refresh carousel on window resize
          $(window).on('resize', function() {
            carousel.trigger('refresh.owl.carousel');
          });
        } else {
          console.log('jQuery or Owl Carousel not loaded');
        }
      }, 1000);
    }])
    .controller('LoginCtrl', ['$http', '$location', function($http, $location){
      var vm = this;
      vm.form = { name: '', password: '' };
      vm.login = function(){
        $http.post('/api/auth/login', vm.form).then(function(){
          localStorage.setItem('isLoggedIn', 'true');
          localStorage.setItem('userName', vm.form.name);
          alert('Login successful');
          $location.path('/app');
        }, function(){
          alert('Login failed: invalid credentials');
        });
      };
    }])
    .controller('RegisterCtrl', ['$http', '$location', function($http, $location){
      var vm = this;
      vm.form = { name: '', password: '' };
      vm.register = function(){
        $http.post('/api/auth/register', vm.form).then(function(){
          alert('Registration successful. Please login.');
          $location.path('/login');
        }, function(err){
          var msg = (err && err.data && err.data.message) || 'Registration failed! Please enter both name and password';
          alert('Registration failed: ' + msg);
        });
      };
    }])
    .controller('MainCtrl', ['$http', '$sce', function($http, $sce){
      var vm = this;
      vm.form = { title: '', code: '', language: 'JAVA' };
      vm.snippetsGuest = [];
      vm.snippetsUser = [];
      vm.snippetsAll = [];
      vm.snippetsShared = [];
      vm.languages = [];
      vm.isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
      vm.editingSnippet = null;
      vm.showShareMenu = false;
      vm.share = { users: [], selected: [] };

      vm.load = function(){
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        var userName = localStorage.getItem('userName') || '';

        function updateAll(){
          var combined = (vm.snippetsGuest || []).concat(vm.snippetsUser || []);
          vm.snippetsAll = combined.sort(function(a,b){
            return new Date(b.createdAt) - new Date(a.createdAt);
          });
        }

        $http.get('/api/snippets/guest').then(function(res){
          vm.snippetsGuest = Array.isArray(res.data) ? res.data : [];
          updateAll();
        });

        if (isLoggedIn && userName) {
          $http.get('/api/snippets/me', { headers: { 'X-User-Name': userName } })
            .then(function(res){
              vm.snippetsUser = Array.isArray(res.data) ? res.data : [];
              updateAll();
            });
          
          // Load shared snippets
          $http.get('/api/snippets/shared/me', { headers: { 'X-User-Name': userName } })
            .then(function(res){
              vm.snippetsShared = Array.isArray(res.data) ? res.data : [];
            });

          // Load users for sharing (exclude self on client side)
          $http.get('/api/auth/users').then(function(res){
            var list = Array.isArray(res.data) ? res.data : [];
            vm.share.users = list.filter(function(u){ return u !== userName; });
          });
        } else {
          vm.snippetsUser = [];
          vm.snippetsShared = [];
          updateAll();
        }

        $http.get('/api/snippets/languages').then(function(res){ vm.languages = res.data; });
      };

      vm.prismAlias = function(lang){
        var map = {
          JAVA: 'java', JAVASCRIPT: 'javascript', TYPESCRIPT: 'typescript', PYTHON: 'python', GO: 'go', CSHARP: 'csharp', CPP: 'cpp', HTML: 'markup', CSS: 'css', SQL: 'sql', JSON: 'json', YAML: 'yaml', SHELL: 'bash'
        };
        return map[lang] || 'clike';
      };

      vm.highlight = function(s){
        var alias = vm.prismAlias(s.language);
        var highlighted = Prism.highlight(s.code, Prism.languages[alias] || Prism.languages.clike, alias);
        return $sce.trustAsHtml(highlighted);
      };

      vm.create = function(){
        var payload = { title: vm.form.title, code: vm.form.code, language: vm.form.language };
        var headers = {};
        var isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        var userName = localStorage.getItem('userName') || '';
        if (isLoggedIn && userName) { headers['X-User-Name'] = userName; }
        $http.post('/api/snippets', payload, { headers: headers }).then(function(){
          alert('Snippet added successfully');
          vm.form.code = '';
          vm.form.title = '';
          vm.load();
        }, function(err){
          console.error('Create failed', err);
          alert('Failed to add snippet');
        });
      };

      vm.remove = function(id){
        $http.delete('/api/snippets/' + encodeURIComponent(id))
          .then(function(){
            // Reload from server to ensure consistency
            vm.load();
          })
          .catch(function(error){
            console.error('Failed to delete snippet:', error);
            alert('Delete failed: ' + (error.status || '') + ' ' + (error.statusText || ''));
          });
      };

      vm.openShareMenu = function(){
        vm.showShareMenu = true;
        vm.share.forSnippet = null;
      };

      vm.openShareFor = function(snippet){
        vm.showShareMenu = true;
        vm.share.forSnippet = snippet;
      };

      vm.cancelShare = function(){
        vm.showShareMenu = false;
        vm.share.selected = [];
      };

      vm.performShare = function(){
        var createPayload = { title: vm.form.title, code: vm.form.code, language: vm.form.language };
        var headers = {};
        var userName = localStorage.getItem('userName') || '';
        if (vm.isLoggedIn && userName) { headers['X-User-Name'] = userName; }

        var recipients = { userNames: vm.share.selected };
        var promise;
        if (vm.share.forSnippet && vm.share.forSnippet.id) {
          // share existing snippet
          promise = $http.post('/api/snippets/' + vm.share.forSnippet.id + '/share-to', recipients, { headers: headers });
        } else {
          // create then share
          promise = $http.post('/api/snippets', createPayload, { headers: headers }).then(function(res){
            var created = res.data;
            return $http.post('/api/snippets/' + created.id + '/share-to', recipients, { headers: headers });
          });
        }

        promise.then(function(){
          alert('Snippet shared successfully');
          vm.form.code = '';
          vm.form.title = '';
          vm.share.selected = [];
          vm.share.forSnippet = null;
          vm.showShareMenu = false;
          vm.load();
        }).catch(function(err){
          console.error('Share failed', err);
          alert('Failed to share snippet');
        });
      };

      vm.editSnippet = function(snippet){
        vm.editingSnippet = snippet;
        vm.form.title = snippet.title;
        vm.form.code = snippet.code;
        vm.form.language = snippet.language;
        
        // Scroll to top to show the form
        window.scrollTo(0, 0);
      };

      vm.updateSnippet = function(){
        if (!vm.editingSnippet) return;
        
        var payload = { title: vm.form.title, code: vm.form.code, language: vm.form.language };
        var headers = {};
        var userName = localStorage.getItem('userName') || '';
        if (vm.isLoggedIn && userName) { headers['X-User-Name'] = userName; }
        
        $http.put('/api/snippets/' + vm.editingSnippet.id, payload, { headers: headers }).then(function(){
          alert('Snippet updated successfully');
          vm.form.code = '';
          vm.form.title = '';
          vm.editingSnippet = null;
          vm.load();
        }, function(err){
          console.error('Update failed', err);
          var msg = (err && err.data && err.data.message) || 'Failed to update snippet';
          alert(msg);
        });
      };

      vm.cancelEdit = function(){
        vm.editingSnippet = null;
        vm.form.code = '';
        vm.form.title = '';
        vm.form.language = 'JAVA';
      };

      vm.copyCode = function(snippet){
        if (!navigator.clipboard) {
          var ta = document.createElement('textarea');
          ta.value = snippet.code || '';
          document.body.appendChild(ta);
          ta.select();
          try { document.execCommand('copy'); alert('Code copied'); } catch(e){ alert('Copy failed'); }
          document.body.removeChild(ta);
          return;
        }
        navigator.clipboard.writeText(snippet.code || '').then(function(){
          alert('Code copied');
        }, function(){ alert('Copy failed'); });
      };

      vm.downloadCode = function(snippet){
        var extMap = { JAVA:'java', JAVASCRIPT:'js', TYPESCRIPT:'ts', PYTHON:'py', GO:'go', CSHARP:'cs', CPP:'cpp', HTML:'html', CSS:'css', SQL:'sql', JSON:'json', YAML:'yml', SHELL:'sh' };
        var ext = extMap[snippet.language] || 'txt';
        var filename = (snippet.title || 'snippet') + '.' + ext;
        var blob = new Blob([snippet.code || ''], { type: 'text/plain;charset=utf-8' });
        var url = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      };

      vm.load();
    }]);
})();


