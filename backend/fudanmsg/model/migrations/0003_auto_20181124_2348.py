# Generated by Django 2.1.3 on 2018-11-24 15:48

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('model', '0002_auto_20181124_2345'),
    ]

    operations = [
        migrations.AlterField(
            model_name='wall_art',
            name='update_time',
            field=models.DateTimeField(auto_now_add=True),
        ),
    ]
