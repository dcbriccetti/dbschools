# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0006_auto_20150704_2040'),
    ]

    operations = [
        migrations.RenameField(
            model_name='section',
            old_name='duration',
            new_name='duration_per_day',
        ),
    ]
