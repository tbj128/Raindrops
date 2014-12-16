<?php
	if (isset($username)) {
?> 
	  <nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="index">Raindrops</a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse navbar-ex1-collapse">
          <ul class="nav navbar-nav side-nav">
            <li <?php echo ($page == 'Dashboard' ? 'class="active"' : ''); ?>><a href="index"><i class="fa fa-home"></i> Dashboard</a></li>
			<?php 
				if ($username == $admin_user) {
			?>
			<li><a href="manager"><i class="fa fa-folder"></i> Content Manager</a></li>
			<?php
				}
			?>
			<li style="width:100%;background-color:#464646;height:1px;"></li>
			<li <?php echo ($page == 'Compose' ? 'class="active"' : ''); ?>><a href="compose"><i class="fa fa-pencil"></i> Compose Message</a></li>
            <li <?php echo ($page == 'Inbox' ? 'class="active"' : ''); ?>><a href="inbox"><i class="fa fa-envelope"></i> Inbox (<?php echo $num_unread; ?>)</a></li>
            <li <?php echo ($page == 'Outbox' ? 'class="active"' : ''); ?>><a href="outbox"><i class="fa fa-envelope"></i> Outbox</a></li>
			<li style="width:100%;background-color:#464646;height:1px;"></li>
			<?php
				foreach ($children as $trainer) {
					$trainer_name = $users[$trainer];
					echo '<li ' . ($page == $trainer_name ? 'class="active"' : '') . '><a href="user?id=' . $trainer . '"><i class="fa fa-user"></i>&nbsp;&nbsp;' . $trainer_name . '</a></li>';
				}
			?>
          </ul>

          <ul class="nav navbar-nav navbar-right navbar-user">
            <li class="dropdown messages-dropdown">
              <a href="inbox"><i class="fa fa-envelope"></i> Messages <span class="badge"><?php echo $num_unread; ?></span></a>
              
            <li class="dropdown user-dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-user"></i> <?php echo $username; ?> <b class="caret"></b></a>
              <ul class="dropdown-menu">
                <!--<li><a href="#"><i class="fa fa-user"></i> Profile</a></li>-->
                <li><a href="inbox"><i class="fa fa-envelope"></i> Inbox <span class="badge"><?php echo $num_unread; ?></span></a></li>
                <!--<li><a href="#"><i class="fa fa-gear"></i> Settings</a></li>-->
                <li class="divider"></li>
                <li><a href="logout"><i class="fa fa-power-off"></i> Log Out</a></li>
              </ul>
            </li>
          </ul>
        </div><!-- /.navbar-collapse -->
      </nav>
<?php
	}
?>