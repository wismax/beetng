<?php 
	defined('_JEXEC') or die('Restricted access'); 

	if (($this->user->authorize('com_content', 'edit', 'content', 'all') || $this->user->authorize('com_content', 'edit', 'content', 'own')) && !$this->print) : 
		?><div class="contentpaneopen_edit<?php echo $this->params->get( 'pageclass_sfx' ); ?>" >
			<?php echo JHTML::_('icon.edit', $this->article, $this->params, $this->access); ?>
		</div><?php 
	endif;

	if (!$this->params->get('show_intro')) :
		echo $this->article->event->afterDisplayTitle;
	endif;

	if (($this->params->get('show_create_date'))
		|| (($this->params->get('show_author')) && ($this->article->author != ""))
		|| (($this->params->get('show_section') && $this->article->sectionid) || ($this->params->get('show_category') && $this->article->catid))
		|| ($this->params->get('show_pdf_icon') || $this->params->get('show_print_icon') || $this->params->get('show_email_icon'))
		|| ($this->params->get('show_url') && $this->article->urls)) :

		?><div class="article-tools">
		<div class="article-meta"><?php 

		if ($this->params->get('show_create_date')) : 
			?><span class="createdate">
				<?php echo JHTML::_('date', $this->article->created, JText::_('DATE_FORMAT_LC2')) ?>
			</span><?php 
		endif; 
		
		if (($this->params->get('show_author')) && ($this->article->author != "")) : 
			?><span class="createby"><?php 
				JText::printf(($this->article->created_by_alias ? $this->article->created_by_alias : $this->article->author) ); 
			?></span><?php 
		endif;

		if (($this->params->get('show_section') 
			&& $this->article->sectionid) 
			|| ($this->params->get('show_category') 
				&& $this->article->catid)) : 

			if ($this->params->get('show_section') 
				&& $this->article->sectionid && isset($this->article->section)) : 
				?><span class="article-section"><?php 
				
				if ($this->params->get('link_section')) : 
					echo '<a href="'.JRoute::_(ContentHelperRoute::getSectionRoute($this->article->sectionid)).'">'; 
				endif;
	
				echo $this->article->section;

				if ($this->params->get('link_section')) :
					echo '</a>';
				endif;

				if ($this->params->get('show_category')) :
					echo ' - ';
				endif; 
					
				?></span><?php 
			endif;

			if ($this->params->get('show_category') && $this->article->catid) : 
				
				?><span class="article-section"><?php 

				if ($this->params->get('link_category')) : 
					echo '<a href="'.JRoute::_(ContentHelperRoute::getCategoryRoute($this->article->catslug, $this->article->sectionid)).'">'; 
				endif;

				echo $this->article->category; 
				
				if ($this->params->get('link_category')) :
					echo '</a>';
				endif; 

				?></span><?php 
			endif;

		endif; 
	
		?></div><?php 
	
	if ($this->params->get('show_pdf_icon') 
		|| $this->params->get('show_print_icon') 
		|| $this->params->get('show_email_icon')) : 
		
		?><div class="buttonheading"><?php 

		if (!$this->print) : 
			if ($this->params->get('show_email_icon')) : 
				?><span><?php 
				echo JHTML::_('icon.email',  $this->article, $this->params, $this->access); 
				?></span><?php 
			endif; 

			if ( $this->params->get( 'show_print_icon' )) : 
				?><span><?php 
				echo JHTML::_('icon.print_popup',  $this->article, $this->params, $this->access); 
				?></span><?php 
			endif; 

			if ($this->params->get('show_pdf_icon')) : 
				?><span><?php 
				echo JHTML::_('icon.pdf',  $this->article, $this->params, $this->access); 
				?></span><?php 
			endif; 
		else : 
			?><span><?php 
			echo JHTML::_('icon.print_screen',  $this->article, $this->params, $this->access); 
			?></span><?php 
		endif; 

	?></div><?php 

	endif; 

	if ($this->params->get('show_url') && $this->article->urls) : 

		?><span class="article-url">
			<a href="http://<?php echo $this->article->urls ; ?>" target="_blank">
				<?php echo $this->article->urls; ?></a>
		</span><?php 
	endif; 
	
	?></div><?php 
endif; 

echo $this->article->event->beforeDisplayContent; 

?><div class="article-content"><?php 

if (isset ($this->article->toc)) : 
	echo $this->article->toc; 
endif; 

echo $this->article->text; 

?></div>
<?php 

if ( intval($this->article->modified) !=0 && $this->params->get('show_modify_date')) : 
	?><span class="modifydate"><?php 
		echo JText::sprintf('LAST_UPDATED2', JHTML::_('date', $this->article->modified, JText::_('DATE_FORMAT_LC2'))); 
	?></span><?php 
endif; 

echo $this->article->event->afterDisplayContent; ?>